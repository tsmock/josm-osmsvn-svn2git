/*
  Copyright 2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.interaction.actions;

/**
  Action options.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public enum OptionsEnum
{
  // <class>
  // <static>
  // <fields>
  /**
    Whether to open the destination document in a new window.
  */
  NewWindow(0x1),
  /**
    Whether to open the destination document in the current window.
  */
  SameWindow(0x2);
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  /**
    <h3>Remarks</h3>
    <p>Bitwise code MUST be explicitly distinct from the ordinal position of the enum constant
    as they don't coincide.</p>
  */
  private final int code;
  // </fields>

  // <constructors>
  private OptionsEnum(
    int code
    )
  {this.code = code;}
  // </constructors>

  // <interface>
  // <public>
  public int getCode(
    )
  {return code;}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}