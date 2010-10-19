/*
  Copyright 2007-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)

  This file should be part of the source code distribution of "PDF Clown library"
  (the Program): see the accompanying README files for more info.

  This Program is free software; you can redistribute it and/or modify it under the terms
  of the GNU Lesser General Public License as published by the Free Software Foundation;
  either version 3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  either expressed or implied; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this
  Program (see README files); if not, go to the GNU website (http://www.gnu.org/licenses/).

  Redistribution and use, with or without modification, are permitted provided that such
  redistributions retain the above copyright notice, license and disclaimer, along with
  this list of conditions.
*/

package it.stefanochizzolini.clown.documents.contents.objects;

import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfReal;

import java.util.List;

/**
  'Append a rectangle to the current path as a complete subpath' operation
  [PDF:1.6:4.4.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public final class DrawRectangle
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  public static final String Operator = "re";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public DrawRectangle(
    double x,
    double y,
    double width,
    double height
    )
  {
    super(
      Operator,
      new PdfReal(x),
      new PdfReal(y),
      new PdfReal(width),
      new PdfReal(height)
      );
  }

  public DrawRectangle(
    List<PdfDirectObject> operands
    )
  {super(Operator,operands);}
  // </constructors>

  // <interface>
  // <public>
  public double getHeight(
    )
  {return ((IPdfNumber)operands.get(3)).getNumberValue();}

  public double getWidth(
    )
  {return ((IPdfNumber)operands.get(2)).getNumberValue();}

  public double getX(
    )
  {return ((IPdfNumber)operands.get(0)).getNumberValue();}

  public double getY(
    )
  {return ((IPdfNumber)operands.get(1)).getNumberValue();}

  /**
    @since 0.0.6
  */
  public void setHeight(
    double value
    )
  {((IPdfNumber)operands.get(3)).setNumberValue(value);}

  /**
    @since 0.0.6
  */
  public void setWidth(
    double value
    )
  {((IPdfNumber)operands.get(2)).setNumberValue(value);}

  /**
    @since 0.0.6
  */
  public void setX(
    double value
    )
  {((IPdfNumber)operands.get(0)).setNumberValue(value);}

  /**
    @since 0.0.6
  */
  public void setY(
    double value
    )
  {((IPdfNumber)operands.get(1)).setNumberValue(value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}