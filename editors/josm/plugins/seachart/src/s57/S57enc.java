/* Copyright 2015 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import s57.S57dat.*;
import s57.S57map.*;

public class S57enc { // S57 ENC file generation
	
	private static byte[] header = {
		// Record 0
		'0', '1', '9', '5', '9', '3', 'L', 'E', '1', ' ', '0', '9', '0', '0', '2', '4', '5', ' ', '!', ' ', '3', '4', '0', '4', // Leader
		'0', '0', '0', '0', '1', '6', '7', '0', '0', '0', '0',    '0', '0', '0', '1', '0', '4', '3', '0', '1', '6', '7', // Directory
		'D', 'S', 'I', 'D', '1', '6', '5', '0', '2', '1', '0',    'D', 'S', 'S', 'I', '1', '1', '3', '0', '3', '7', '5',
		'D', 'S', 'P', 'M', '1', '3', '0', '0', '4', '8', '8',    'F', 'R', 'I', 'D', '1', '0', '0', '0', '6', '1', '8',
		'F', 'O', 'I', 'D', '0', '7', '0', '0', '7', '1', '8',    'A', 'T', 'T', 'F', '0', '5', '9', '0', '7', '8', '8',
		'N', 'A', 'T', 'F', '0', '6', '8', '0', '8', '4', '7',    'F', 'F', 'P', 'C', '0', '9', '0', '0', '9', '1', '5',
		'F', 'F', 'P', 'T', '0', '8', '6', '1', '0', '0', '5',    'F', 'S', 'P', 'C', '0', '9', '0', '1', '0', '9', '1',
		'F', 'S', 'P', 'T', '0', '8', '9', '1', '1', '8', '1',    'V', 'R', 'I', 'D', '0', '7', '8', '1', '2', '7', '0',
		'A', 'T', 'T', 'V', '0', '5', '8', '1', '3', '4', '8',    'V', 'R', 'P', 'C', '0', '7', '1', '1', '4', '0', '6',
		'V', 'R', 'P', 'T', '0', '7', '6', '1', '4', '7', '7',    'S', 'G', 'C', 'C', '0', '6', '0', '1', '5', '5', '3',
		'S', 'G', '2', 'D', '0', '4', '8', '1', '6', '1', '3',    'S', 'G', '3', 'D', '0', '5', '3', '1', '6', '6', '1', 0x1e,
		 // File control field
		'0', '0', '0', '0', ';', '&', ' ', ' ', ' ',
		//*** 254
		'0', 'S', '0', '0', '0', '0', '0', '0', '.', '0', '0', '0', 0x1f, // Filename
		'0', '0', '0', '1', 'D', 'S', 'I', 'D',  'D', 'S', 'I', 'D', 'D', 'S', 'S', 'I',  '0', '0', '0', '1', 'D', 'S', 'P', 'M',
		'0', '0', '0', '1', 'F', 'R', 'I', 'D',  'F', 'R', 'I', 'D', 'F', 'O', 'I', 'D',  'F', 'R', 'I', 'D', 'A', 'T', 'T', 'F',
		'F', 'R', 'I', 'D', 'N', 'A', 'T', 'F',  'F', 'R', 'I', 'D', 'F', 'F', 'P', 'C',  'F', 'R', 'I', 'D', 'F', 'F', 'P', 'T',
		'F', 'R', 'I', 'D', 'F', 'S', 'P', 'C',  'F', 'R', 'I', 'D', 'F', 'S', 'P', 'T',  '0', '0', '0', '1', 'V', 'R', 'I', 'D',
		'V', 'R', 'I', 'D', 'A', 'T', 'T', 'V',  'V', 'R', 'I', 'D', 'V', 'R', 'P', 'C',  'V', 'R', 'I', 'D', 'V', 'R', 'P', 'T',
		'V', 'R', 'I', 'D', 'S', 'G', 'C', 'C',  'V', 'R', 'I', 'D', 'S', 'G', '2', 'D',  'V', 'R', 'I', 'D', 'S', 'G', '3', 'D', 0x1e,
		 // Record identifier fields
		'0', '5', '0', '0', ';', '&', ' ', ' ', ' ', 'I', 'S', 'O', ' ', '8', '2', '1', '1', ' ', 'R', 'e', 'c', 'o', 'r', 'd',
		' ', 'I', 'd', 'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', 0x1f, 0x1f, '(', 'b', '1', '2', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'D', 'a', 't', 'a', ' ', 'S', 'e', 't', ' ', 'I', 'd', 'e', 'n', 't', 'i', 'f',
		'i', 'c', 'a', 't', 'i', 'o', 'n', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'E',
		'X', 'P', 'P', '!', 'I', 'N', 'T', 'U', '!', 'D', 'S', 'N', 'M', '!', 'E', 'D', 'T', 'N', '!', 'U', 'P', 'D', 'N', '!', 'U',
		'A', 'D', 'T', '!', 'I', 'S', 'D', 'T', '!', 'S', 'T', 'E', 'D', '!', 'P', 'R', 'S', 'P', '!', 'P', 'S', 'D', 'N', '!', 'P',
		'R', 'E', 'D', '!', 'P', 'R', 'O', 'F', '!', 'A', 'G', 'E', 'N', '!', 'C', 'O', 'M', 'T', 0x1f, '(', 'b', '1', '1', ',', 'b',
		'1', '4', ',', '2', 'b', '1', '1', ',', '3', 'A', ',', '2', 'A', '(', '8', ')', ',', 'R', '(', '4', ')', ',', 'b', '1', '1',
		',', '2', 'A', ',', 'b', '1', '1', ',', 'b', '1', '2', ',', 'A', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'D', 'a', 't', 'a', ' ', 'S', 'e', 't', ' ', 'S', 't', 'r', 'u', 'c', 't', 'u',
		'r', 'e', ' ', 'I', 'n', 'f', 'o', 'r', 'm', 'a', 't', 'i', 'o', 'n', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'D', 'S', 'T', 'R',
		'!', 'A', 'A', 'L', 'L', '!', 'N', 'A', 'L', 'L', '!', 'N', 'O', 'M', 'R', '!', 'N', 'O', 'C', 'R', '!', 'N', 'O', 'G', 'R',
		'!', 'N', 'O', 'L', 'R', '!', 'N', 'O', 'I', 'N', '!', 'N', 'O', 'C', 'N', '!', 'N', 'O', 'E', 'D', '!', 'N', 'O', 'F', 'A', 0x1f,
		'(', '3', 'b', '1', '1', ',', '8', 'b', '1', '4', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'D', 'a', 't', 'a', ' ', 'S', 'e', 't', ' ', 'P', 'a', 'r', 'a', 'm', 'e', 't',
		'e', 'r', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'H', 'D', 'A', 'T', '!', 'V',
		'D', 'A', 'T', '!', 'S', 'D', 'A', 'T', '!', 'C', 'S', 'C', 'L', '!', 'D', 'U', 'N', 'I', '!', 'H', 'U', 'N', 'I', '!', 'P',
		'U', 'N', 'I', '!', 'C', 'O', 'U', 'N', '!', 'C', 'O', 'M', 'F', '!', 'S', 'O', 'M', 'F', '!', 'C', 'O', 'M', 'T', 0x1f,
		'(', 'b', '1', '1', ',', 'b', '1', '4', ',', '3', 'b', '1', '1', ',', 'b', '1', '4', ',', '4', 'b', '1', '1', ',', '2', 'b',
		'1', '4', ',', 'A', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'I',
		'd', 'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D',
		'!', 'P', 'R', 'I', 'M', '!', 'G', 'R', 'U', 'P', '!', 'O', 'B', 'J', 'L', '!', 'R', 'V', 'E', 'R', '!', 'R', 'U', 'I', 'N', 0x1f,
		'(', 'b', '1', '1', ',', 'b', '1', '4', ',', '2', 'b', '1', '1', ',', '2', 'b', '1', '2', ',', 'b', '1', '1', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'O', 'b', 'j', 'e', 'c', 't', ' ', 'I',
		'd', 'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'A', 'G', 'E', 'N', '!', 'F', 'I', 'D', 'N',
		'!', 'F', 'I', 'D', 'S', 0x1f, '(', 'b', '1', '2', ',', 'b', '1', '4', ',', 'b', '1', '2', ')', 0x1e,
		'2', '6', '0', '0', ';', '&', '-', 'A', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'A',
		't', 't', 'r', 'i', 'b', 'u', 't', 'e', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, '*', 'A', 'T', 'T', 'L', '!', 'A', 'T', 'V', 'L', 0x1f,
		'(', 'b', '1', '2', ',', 'A', ')', 0x1e,
		'2', '6', '0', '0', ';', '&', '-', 'A', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'N',
		'a', 't', 'i', 'o', 'n', 'a', 'l', ' ', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f,
		'*', 'A', 'T', 'T', 'L', '!', 'A', 'T', 'V', 'L', 0x1f, '(', 'b', '1', '2', ',', 'A', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'T',
		'o', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'O', 'b', 'j', 'e', 'c', 't', ' ', 'P', 'o', 'i', 'n', 't', 'e', 'r', ' ',
		'C', 'o', 'n', 't', 'r', 'o', 'l', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'F', 'F', 'U', 'I', '!', 'F', 'F', 'I', 'X', '!', 'N',
		'F', 'P', 'T', 0x1f, '(', 'b', '1', '1', ',', '2', 'b', '1', '2', ')', 0x1e,
		'2', '0', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'T',
		'o', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'O', 'b', 'j', 'e', 'c', 't', ' ', 'P', 'o', 'i', 'n', 't', 'e', 'r', ' ', 
		'F', 'i', 'e', 'l', 'd', 0x1f, '*', 'L', 'N', 'A', 'M', '!', 'R', 'I', 'N', 'D', '!', 'C', 'O', 'M', 'T', 0x1f, '(', 'B', '(',
		'6', '4', ')', ',', 'b', '1', '1', ',', 'A', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ',	' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'T',
		'o', ' ', 'S', 'p', 'a', 't', 'i', 'a', 'l', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'P', 'o', 'i', 'n', 't', 'e', 'r', ' ',
		'C', 'o', 'n', 't', 'r', 'o', 'l', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'F', 'S', 'U', 'I', '!', 'F', 'S', 'I', 'X', '!', 'N',
		'S', 'P', 'T', 0x1f, '(', 'b', '1', '1', ',', '2', 'b', '1', '2', ')', 0x1e,
		'2', '0', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'T', 
		'o', ' ', 'S', 'p', 'a', 't', 'i', 'a', 'l', ' ', 'R', 'e', 'c', 'o', 'r', ' ', 'P', 'o', 'i', 'n', 't', 'e', 'r', ' ', 'F',
		'i', 'e', 'l', 'd', 0x1f, '*', 'N', 'A', 'M', 'E', '!', 'O', 'R', 'N', 'T', '!', 'U', 'S', 'A', 'G', '!', 'M', 'A', 'S', 'K', 0x1f,
		'(', 'B', '(', '4', '0', ')', ',', '3', 'b', '1', '1', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'I', 'd',
		'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!',
		'R', 'V', 'E', 'R', '!', 'R', 'U', 'I', 'N', 0x1f, '(', 'b', '1', '1', ',', 'b', '1', '4', ',', 'b', '1', '2', ',', 'b', '1',
		'1', ')', 0x1e,
		'2', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'A', 't',
		't', 'r', 'i', 'b', 'u', 't', 'e', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, '*', 'A', 'T', 'T', 'L', '!', 'A', 'T', 'V', 'L', 0x1f,
		'(', 'b', '1', '2', ',', 'A', ')', 0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'P', 'o',
		'i', 'n', 't', 'e', 'r', ' ', 'C', 'o', 'n', 't', 'r', 'o', 'l', ' ', 'F', 'i', 'e', 'l', 's', 0x1f, 'V', 'P', 'U', 'I', '!',
		'V', 'P', 'I', 'X', '!', 'N', 'V', 'P', 'T', 0x1f, '(', 'b', '1', '1', ',', '2', 'b', '1', '2', ')', 0x1e,
		'2', '0', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'P', 'o',
		'i', 'n', 't', 'e', 'r', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, '*', 'N', 'A', 'M', 'E', '!', 'O', 'R', 'N', 'T', '!', 'U', 'S',
		'A', 'G', '!', 'T', 'O', 'P', 'I', '!', 'M', 'A', 'S', 'K', 0x1f, '(', 'B', '(', '4', '0', ')', ',', '4', 'b', '1', '1', ')',
		0x1e,
		'1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'C', 'o', 'o', 'r', 'd', 'i', 'n', 'a', 't', 'e', ' ', 'C', 'o', 'n', 't', 'r',
		'o', 'l', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'C', 'C', 'U', 'I', '!', 'C', 'C', 'I', 'X', '!', 'C', 'C', 'N', 'C', 0x1f, '(',
		'b', '1', '1', ',', '2', 'b', '1', '2', ')', 0x1e,
		'2', '2', '0', '0', ';', '&', ' ', ' ', ' ', '2', '-', 'D', ' ', 'C', 'o', 'o', 'r', 'd', 'i', 'n', 'a', 't', 'e', ' ', 'F', 
		'i', 'e', 'l', 'd', 0x1f, '*', 'Y', 'C', 'O', 'O', '!', 'X', 'C', 'O', 'O', 0x1f, '(', '2', 'b', '2', '4', ')',
		0x1e,
		'2', '2', '0', '0', ';', '&', ' ', ' ', ' ', '3', '-', 'D', ' ', 'C', 'o', 'o', 'r', 'd', 'i', 'n', 'a', 't', 'e', ' ', 'F',
		'i', 'e', 'l', 'd', 0x1f, '*', 'Y', 'C', 'O', 'O', '!', 'X', 'C', 'O', 'O', '!', 'V', 'E', '3', 'D', 0x1f, '(', '3', 'b', '2',
		'4', ')', 0x1e,

		// Record 1
		'0', '0', '1', '6', '7', ' ', 'D', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '4', '9', ' ', ' ', ' ', '2', '2', '0', '4', // Leader
		// Directory
		'0', '0', '0', '1', '0', '3', '0', '0',   'D', 'S', 'I', 'D', '7', '9', '0', '3',   'D', 'S', 'S', 'I', '3', '6', '8', '2', 0x1e,
		'1', '0', 0x1e, // Field 0001
		// Field DSID
		0x0a, 0x01, 0x00, 0x00, 0x00, 0x01,
		//*** 2017
		0x00, '0', 'S', '0', '0', '0', '0', '0', '0', '.', '0', '0', '0', 0x1f, // INTU & Filename
		'1', 0x1f, '0', 0x1f,
		//*** 2035
		'0', '0', '0', '0', '0', '0', '0', '0',   '0', '0', '0', '0', '0', '0', '0', '0', // Date x2 (from system)
		'0', '3', '.', '1', 0x14, 0x1f, 0x1f, 0x01, 0x26, 0x0f, 'G', 'e', 'n', 'e', 'r', 'a', 't', 'e', 'd', ' ', 'b', 'y', ' ', 
		'O', 'p', 'e', 'n', 'S', 'e', 'a', 'M', 'a', 'p', '.', 'o', 'r', 'g', 0x1f, 0x1e,
		// Field DSSI
		0x02, 0x01, 0x02,
		//*** 2093
		0x00, 0x00, 0x00, 0x00, // # of meta records (from metas counter)
		0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, // # of geo records (from geos counter)
		0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, // # of isolated node records (from isols counter)
		0x00, 0x00, 0x00, 0x00, // # of connected node records (from conns counter)
		0x00, 0x00, 0x00, 0x00, // # of edge records (from edges counter)
		0x00, 0x00, 0x00, 0x00, 0x1e,

		// Record 2
		'0', '0', '0', '6', '8', ' ', 'D', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '3', '9', ' ', ' ', ' ', '2', '1', '0', '4',
		'0', '0', '0', '1', '0', '3', '0', 'D', 'S', 'P', 'M', '2', '6', '3', 0x1e,
		// Field 0001
		0x02, 0x00, 0x1e,
		// Field DSPM
		0x14, 0x01, 0x00, 0x00, 0x00, 0x02, 0x17, 0x17,
		//*** 2176
		0x00, 0x00, 0x00, 0x00, // Scale (from meta list)
		0x01, // Depth units (from meta list)
		0x01, // Height units (from meta list)
		0x01, 0x01,
		(byte)0x80, (byte)0x96, (byte)0x98, 0x00, // COMF=10000000
		0x0a, // SOMF=10
		0x00, 0x00, 0x00, 0x1f, 0x1e
	};
	
	static final double COMF=10000000;
	static final double SOMF=10;
	
	static int idx;
	static int recs;
	static int isols;
	static int conns;
	static int metas;
	static int geos;
	static int rcid;

	public static int encodeChart(S57map map, HashMap<String, String> meta, byte[] buf) throws IndexOutOfBoundsException, UnsupportedEncodingException {

		/*
		 * Encoding order:
		 * 1. Copy records 0-3 & fill in meta attributes.
		 * 2. Depth isolated nodes.
		 * 3. All other isolated nodes.
		 * 4. Connected nodes.
		 * 5. Edges.
		 * 6. Meta objects.
		 * 7. Geo objects.
		 */
		
		recs = rcid = 3;
		isols = conns = metas = geos = 0;
		for (idx = 0; idx < header.length; idx++) {
			buf[idx] = header[idx];
		}
//		byte[] file = S57dat.encSubf(S57subf.FILE, meta.get("FILE"));
		
		for (Map.Entry<Long, S57map.Snode> entry : map.nodes.entrySet()) {
			S57map.Snode node = entry.getValue();
			if (node.flg == Nflag.ISOL) {
				byte[] record = S57dat.encRecord(recs++, S57record.VI, rcid++, 1, 1, (Math.toDegrees(node.lat) * COMF), (Math.toDegrees(node.lon) * COMF));
				System.arraycopy(record, 0, buf, idx, record.length);
				idx += record.length;
				isols++;
				recs++;
			}
		}

	return idx;
}

	public static int encodeCatalogue(S57map map, byte[] buf) throws IndexOutOfBoundsException {
		
	return idx;
}

}
