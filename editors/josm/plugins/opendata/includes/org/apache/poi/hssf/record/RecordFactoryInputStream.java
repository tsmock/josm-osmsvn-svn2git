/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hssf.record;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A stream based way to get at complete records, with
 * as low a memory footprint as possible.
 * This handles reading from a RecordInputStream, turning
 * the data into full records, processing continue records
 * etc.
 * Most users should use {@link HSSFEventFactory} /
 * {@link HSSFListener} and have new records pushed to
 * them, but this does allow for a "pull" style of coding.
 */
public final class RecordFactoryInputStream {

    /**
     * Keeps track of the sizes of the initial records up to and including {@link FilePassRecord}
     * Needed for protected files because each byte is encrypted with respect to its absolute
     * position from the start of the stream.
     */
    private static final class StreamEncryptionInfo {
        private final Record _lastRecord;
        private final boolean _hasBOFRecord;

        public StreamEncryptionInfo(RecordInputStream rs, List<Record> outputRecs) {
            Record rec;
            rs.nextRecord();
            rec = RecordFactory.createSingleRecord(rs);
            outputRecs.add(rec);
            if (rec instanceof BOFRecord) {
                _hasBOFRecord = true;
                if (rs.hasNextRecord()) {
                    rs.nextRecord();
                    rec = RecordFactory.createSingleRecord(rs);
                    outputRecs.add(rec);
                    if (rec instanceof FilePassRecord) {
                        outputRecs.remove(outputRecs.size()-1);
                        // TODO - add fpr not added to outputRecs
                        rec = outputRecs.get(0);
                    } else {
                        // workbook not encrypted (typical case)
                        if (rec instanceof EOFRecord) {
                            // A workbook stream is never empty, so crash instead
                            // of trying to keep track of nesting level
                            throw new IllegalStateException("Nothing between BOF and EOF");
                        }
                    }
                }
            } else {
                // Invalid in a normal workbook stream.
                // However, some test cases work on sub-sections of
                // the workbook stream that do not begin with BOF
                _hasBOFRecord = false;
            }
            _lastRecord = rec;
        }

        /**
         * @return last record scanned while looking for encryption info.
         * This will typically be the first or second record read. Possibly <code>null</code>
         * if stream was empty
         */
        public Record getLastRecord() {
            return _lastRecord;
        }

        /**
         * <code>false</code> in some test cases
         */
        public boolean hasBOFRecord() {
            return _hasBOFRecord;
        }
    }


    private final RecordInputStream _recStream;
    private final boolean _shouldIncludeContinueRecords;

    /**
     * Temporarily stores a group of {@link Record}s, for future return by {@link #nextRecord()}.
     * This is used at the start of the workbook stream, and also when the most recently read
     * underlying record is a {@link MulRKRecord}
     */
    private Record[] _unreadRecordBuffer;

    /**
     * used to help iterating over the unread records
     */
    private int _unreadRecordIndex = -1;

    /**
     * The most recent record that we gave to the user
     */
    private Record _lastRecord = null;

    private int _bofDepth;

    private boolean _lastRecordWasEOFLevelZero;


    /**
     * @param shouldIncludeContinueRecords caller can pass <code>false</code> if loose
     * {@link ContinueRecord}s should be skipped (this is sometimes useful in event based
     * processing).
     */
    public RecordFactoryInputStream(InputStream in, boolean shouldIncludeContinueRecords) {
        RecordInputStream rs = new RecordInputStream(in);
        List<Record> records = new ArrayList<Record>();
        StreamEncryptionInfo sei = new StreamEncryptionInfo(rs, records);

        if (!records.isEmpty()) {
            _unreadRecordBuffer = new Record[records.size()];
            records.toArray(_unreadRecordBuffer);
            _unreadRecordIndex =0;
        }
        _recStream = rs;
        _shouldIncludeContinueRecords = shouldIncludeContinueRecords;
        _lastRecord = sei.getLastRecord();

        /*
        * How to recognise end of stream?
        * In the best case, the underlying input stream (in) ends just after the last EOF record
        * Usually however, the stream is padded with an arbitrary byte count.  Excel and most apps
        * reliably use zeros for padding and if this were always the case, this code could just
        * skip all the (zero sized) records with sid==0.  However, bug 46987 shows a file with
        * non-zero padding that is read OK by Excel (Excel also fixes the padding).
        *
        * So to properly detect the workbook end of stream, this code has to identify the last
        * EOF record.  This is not so easy because the worbook bof+eof pair do not bracket the
        * whole stream.  The worksheets follow the workbook, but it is not easy to tell how many
        * sheet sub-streams should be present.  Hence we are looking for an EOF record that is not
        * immediately followed by a BOF record.  One extra complication is that bof+eof sub-
        * streams can be nested within worksheet streams and it's not clear in these cases what
        * record might follow any EOF record.  So we also need to keep track of the bof/eof
        * nesting level.
        */
        _bofDepth = sei.hasBOFRecord() ? 1 : 0;
        _lastRecordWasEOFLevelZero = false;
    }

    /**
     * Returns the next (complete) record from the
     * stream, or null if there are no more.
     */
    public Record nextRecord() {
        Record r;
        r = getNextUnreadRecord();
        if (r != null) {
            // found an unread record
            return r;
        }
        while (true) {
            if (!_recStream.hasNextRecord()) {
                // recStream is exhausted;
                return null;
            }

            if (_lastRecordWasEOFLevelZero) {
                // Potential place for ending the workbook stream
                // Check that the next record is not BOFRecord(0x0809)
                // Normally the input stream contains only zero padding after the last EOFRecord,
                // but bug 46987 and 48068 suggests that the padding may be garbage.
                // This code relies on the padding bytes not starting with BOFRecord.sid
                if (_recStream.getNextSid() != BOFRecord.sid) {
                    return null;
                }
                // else - another sheet substream starting here
            }

            // step underlying RecordInputStream to the next record
            _recStream.nextRecord();

            r = readNextRecord();
            if (r == null) {
                // some record types may get skipped (e.g. DBCellRecord and ContinueRecord)
                continue;
            }
            return r;
        }
    }

    /**
     * @return the next {@link Record} from the multiple record group as expanded from
     * a recently read {@link MulRKRecord}. <code>null</code> if not present.
     */
    private Record getNextUnreadRecord() {
        if (_unreadRecordBuffer != null) {
            int ix = _unreadRecordIndex;
            if (ix < _unreadRecordBuffer.length) {
                Record result = _unreadRecordBuffer[ix];
                _unreadRecordIndex = ix + 1;
                return result;
            }
            _unreadRecordIndex = -1;
            _unreadRecordBuffer = null;
        }
        return null;
    }

    /**
     * @return the next available record, or <code>null</code> if
     * this pass didn't return a record that's
     * suitable for returning (eg was a continue record).
     */
    private Record readNextRecord() {

        Record record = RecordFactory.createSingleRecord(_recStream);
        _lastRecordWasEOFLevelZero = false;

        if (record instanceof BOFRecord) {
            _bofDepth++;
            return record;
        }

        if (record instanceof EOFRecord) {
            _bofDepth--;
            if (_bofDepth < 1) {
                _lastRecordWasEOFLevelZero = true;
            }

            return record;
        }

        if (record instanceof DBCellRecord) {
            // Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
            return null;
        }

        if (record instanceof RKRecord) {
            return RecordFactory.convertToNumberRecord((RKRecord) record);
        }

        if (record instanceof MulRKRecord) {
            Record[] records = RecordFactory.convertRKRecords((MulRKRecord) record);

            _unreadRecordBuffer = records;
            _unreadRecordIndex = 1;
            return records[0];
        }

        if (record.getSid() == ContinueRecord.sid) {
            if (_lastRecord instanceof ObjRecord || _lastRecord instanceof TextObjectRecord) {
                //we must remember the position of the continue record.
                //in the serialization procedure the original structure of records must be preserved
                if (_shouldIncludeContinueRecords) {
                    return record;
                }
                return null;
            }
            if (_lastRecord instanceof UnknownRecord) {
                //Gracefully handle records that we don't know about,
                //that happen to be continued
                return record;
            }
            if (_lastRecord instanceof EOFRecord) {
                // This is really odd, but excel still sometimes
                //  outputs a file like this all the same
                return record;
            }
            throw new RecordFormatException("Unhandled Continue Record followining " + _lastRecord.getClass());
        }
        _lastRecord = record;
        return record;
    }
}
