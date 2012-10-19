/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/* Generated By:JJTree: Do not edit this line. ASTERNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package edu.upenn.isc.activemqExt.org.apache.commons.jexl2.parser;

import edu.upenn.isc.activemqExt.org.apache.commons.jexl2.parser.JexlNode;
import edu.upenn.isc.activemqExt.org.apache.commons.jexl2.parser.Parser;
import edu.upenn.isc.activemqExt.org.apache.commons.jexl2.parser.ParserVisitor;

public
class ASTERNode extends JexlNode {
  public ASTERNode(int id) {
    super(id);
  }

  public ASTERNode(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=53e5995828f3b48c15feef8aac2ab8f9 (do not edit this line) */