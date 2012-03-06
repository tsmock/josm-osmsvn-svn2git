/*--

 $Id: Document.java,v 1.85 2007/11/10 05:28:58 jhunter Exp $

 Copyright (C) 2000-2007 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 */

package org.jdom;

import java.util.Iterator;
import java.util.List;

/**
 * An XML document. Methods allow access to the root element as well as the
 * {@link DocType} and other document-level information.
 *
 * @version $Revision: 1.85 $, $Date: 2007/11/10 05:28:58 $
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Jools Enticknap
 * @author  Bradley S. Huffman
 */
public class Document implements Parent {

    /**
     * This document's content including comments, PIs, a possible
     * DocType, and a root element.
     * Subclassers have to track content using their own
     * mechanism.
     */
    ContentList content = new ContentList(this);

    /**
     * Creates a new empty document.  A document must have a root element,
     * so this document will not be well-formed and accessor methods will
     * throw an IllegalStateException if this document is accessed before a
     * root element is added.  This method is most useful for build tools.
     */
    public Document() {}

    /**
     * This will create a new <code>Document</code>,
     * with the supplied <code>{@link Element}</code>
     * as the root element, the supplied
     * <code>{@link DocType}</code> declaration, and the specified
     * base URI.
     *
     * @param rootElement <code>Element</code> for document root.
     * @param docType <code>DocType</code> declaration.
     * @param baseURI the URI from which this doucment was loaded.
     * @throws IllegalAddException if the given docType object
     *         is already attached to a document or the given
     *         rootElement already has a parent
     */
    public Document(Element rootElement, DocType docType, String baseURI) {
        if (rootElement != null) {
            setRootElement(rootElement);
        }
        if (docType != null) {
            setDocType(docType);
        }
        if (baseURI != null) {
            setBaseURI(baseURI);
        }
    }

    /**
     * This will create a new <code>Document</code>,
     * with the supplied <code>{@link Element}</code>
     * as the root element, and no <code>{@link DocType}</code>
     * declaration.
     *
     * @param rootElement <code>Element</code> for document root
     * @throws IllegalAddException if the given rootElement already has
     *         a parent.
     */
    public Document(Element rootElement) {
        this(rootElement, null, null);
    }

//    /**
//     * Starting at the given index (inclusive), return the index of
//     * the first child matching the supplied filter, or -1
//     * if none is found.
//     *
//     * @return index of child, or -1 if none found.
//     */
//    private int indexOf(int start, Filter filter) {
//        int size = getContentSize();
//        for (int i = start; i < size; i++) {
//            if (filter.matches(getContent(i))) {
//                return i;
//            }
//        }
//        return -1;
//    }

    /**
     * This will return <code>true</code> if this document has a
     * root element, <code>false</code> otherwise.
     *
     * @return <code>true</code> if this document has a root element,
     *         <code>false</code> otherwise.
     */
    public boolean hasRootElement() {
        return (content.indexOfFirstElement() < 0) ? false : true;
    }

    /**
     * This will return the root <code>Element</code>
     * for this <code>Document</code>
     *
     * @return <code>Element</code> - the document's root element
     * @throws IllegalStateException if the root element hasn't been set
     */
    public Element getRootElement() {
        int index = content.indexOfFirstElement();
        if (index < 0) {
            throw new IllegalStateException("Root element not set");
        }
        return (Element) content.get(index);
    }

    /**
     * This sets the root <code>{@link Element}</code> for the
     * <code>Document</code>. If the document already has a root
     * element, it is replaced.
     *
     * @param rootElement <code>Element</code> to be new root.
     * @return <code>Document</code> - modified Document.
     * @throws IllegalAddException if the given rootElement already has
     *         a parent.
     */
    public Document setRootElement(Element rootElement) {
        int index = content.indexOfFirstElement();
        if (index < 0) {
            content.add(rootElement);
        }
        else {
            content.set(index, rootElement);
        }
        return this;
    }

    /**
     * This will return the <code>{@link DocType}</code>
     * declaration for this <code>Document</code>, or
     * <code>null</code> if none exists.
     *
     * @return <code>DocType</code> - the DOCTYPE declaration.
     */
    public DocType getDocType() {
        int index = content.indexOfDocType();
        if (index < 0) {
            return null;
        }
        else {
            return (DocType) content.get(index);
        }
    }

    /**
     * This will set the <code>{@link DocType}</code>
     * declaration for this <code>Document</code>. Note
     * that a DocType can only be attached to one Document.
     * Attempting to set the DocType to a DocType object
     * that already belongs to a Document will result in an
     * IllegalAddException being thrown.
     *
     * @param docType <code>DocType</code> declaration.
     * @return object on which the method was invoked
     * @throws IllegalAddException if the given docType is
     *   already attached to a Document.
     */
    public Document setDocType(DocType docType) {
        if (docType == null) {
            // Remove any existing doctype
            int docTypeIndex = content.indexOfDocType();
            if (docTypeIndex >= 0) content.remove(docTypeIndex);
            return this;
        }

        if (docType.getParent() != null) {
            throw new IllegalAddException(docType,
                              "The DocType already is attached to a document");
        }

        // Add DocType to head if new, replace old otherwise
        int docTypeIndex = content.indexOfDocType();
        if (docTypeIndex < 0) {
            content.add(0, docType);
        }
        else {
            content.set(docTypeIndex, docType);
        }

        return this;
    }

    /**
     * Appends the child to the end of the content list.
     *
     * @param child   child to append to end of content list
     * @return        the document on which the method was called
     * @throws IllegalAddException if the given child already has a parent.
     */
    public Document addContent(Content child) {
        content.add(child);
        return this;
    }

//    public Content getChild(Filter filter) {
//        int i = indexOf(0, filter);
//        return (i < 0) ? null : getContent(i);
//    }

    /**
     * This will return all content for the <code>Document</code>.
     * The returned list is "live" in document order and changes to it
     * affect the document's actual content.
     *
     * <p>
     * Sequential traversal through the List is best done with a Iterator
     * since the underlying implement of List.size() may require walking the
     * entire list.
     * </p>
     *
     * @return <code>List</code> - all Document content
     * @throws IllegalStateException if the root element hasn't been set
     */
    public List getContent() {
        if (!hasRootElement())
            throw new IllegalStateException("Root element not set");
        return content;
    }

    /**
     *
     * <p>
     * Sets the effective URI from which this document was loaded,
     * and against which relative URLs in this document will be resolved.
     * </p>
     *
     * @param uri the base URI of this document
     */
    public final void setBaseURI(String uri) {
    }

    public boolean removeContent(Content child) {
        return content.remove(child);
    }

    /**
     * This returns a <code>String</code> representation of the
     * <code>Document</code>, suitable for debugging. If the XML
     * representation of the <code>Document</code> is desired,
     * {@link org.jdom.output.XMLOutputter#outputString(Document)}
     * should be used.
     *
     * @return <code>String</code> - information about the
     *         <code>Document</code>
     */
    public String toString() {
        StringBuffer stringForm = new StringBuffer()
            .append("[Document: ");

        DocType docType = getDocType();
        if (docType != null) {
            stringForm.append(docType.toString())
                      .append(", ");
        } else {
            stringForm.append(" No DOCTYPE declaration, ");
        }

        if (hasRootElement()) {
            stringForm.append("Root is ")
                      .append(getRootElement().toString());
        } else {
            stringForm.append(" No root element"); // shouldn't happen
        }

        stringForm.append("]");

        return stringForm.toString();
    }

    /**
     * This tests for equality of this <code>Document</code> to the supplied
     * <code>Object</code>.
     *
     * @param ob <code>Object</code> to compare to
     * @return <code>boolean</code> whether the <code>Document</code> is
     *         equal to the supplied <code>Object</code>
     */
    public final boolean equals(Object ob) {
        return (ob == this);
    }

    /**
     * This returns the hash code for this <code>Document</code>.
     *
     * @return <code>int</code> hash code
     */
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * This will return a deep clone of this <code>Document</code>.
     *
     * @return <code>Object</code> clone of this <code>Document</code>
     */
    public Object clone() {
        Document doc = null;

        try {
            doc = (Document) super.clone();
        } catch (CloneNotSupportedException ce) {
            // Can't happen
        }

        // The clone has a reference to this object's content list, so
        // owerwrite with a empty list
        doc.content = new ContentList(doc);

        // Add the cloned content to clone

        for (int i = 0; i < content.size(); i++) {
            Object obj = content.get(i);
            if (obj instanceof Element) {
                Element element = (Element)((Element)obj).clone();
                doc.content.add(element);
            }
            else if (obj instanceof Comment) {
                Comment comment = (Comment)((Comment)obj).clone();
                doc.content.add(comment);
            }
            else if (obj instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction)
                           ((ProcessingInstruction)obj).clone();
                doc.content.add(pi);
            }
            else if (obj instanceof DocType) {
                DocType dt = (DocType) ((DocType)obj).clone();
                doc.content.add(dt);
            }
        }

        return doc;
    }

    /**
     * Returns an iterator that walks over all descendants in document order.
     *
     * @return an iterator to walk descendants
     */
    public Iterator getDescendants() {
        return new DescendantIterator(this);
    }

    public Parent getParent() {
        return null;  // documents never have parents
    }

    /**
     * @see org.jdom.Parent#getDocument()
     */
    public Document getDocument() {
        return this;
    }
}
