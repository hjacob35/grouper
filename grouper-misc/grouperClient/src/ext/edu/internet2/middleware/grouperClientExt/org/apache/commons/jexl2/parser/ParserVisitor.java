/**
 * Copyright 2014 Internet2
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
 */
/* Generated By:JavaCC: Do not edit this line. ParserVisitor.java Version 5.0 */
package edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.parser;

public interface ParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTJexlScript node, Object data);
  public Object visit(ASTBlock node, Object data);
  public Object visit(ASTAmbiguous node, Object data);
  public Object visit(ASTIfStatement node, Object data);
  public Object visit(ASTWhileStatement node, Object data);
  public Object visit(ASTForeachStatement node, Object data);
  public Object visit(ASTReturnStatement node, Object data);
  public Object visit(ASTAssignment node, Object data);
  public Object visit(ASTVar node, Object data);
  public Object visit(ASTReference node, Object data);
  public Object visit(ASTTernaryNode node, Object data);
  public Object visit(ASTOrNode node, Object data);
  public Object visit(ASTAndNode node, Object data);
  public Object visit(ASTBitwiseOrNode node, Object data);
  public Object visit(ASTBitwiseXorNode node, Object data);
  public Object visit(ASTBitwiseAndNode node, Object data);
  public Object visit(ASTEQNode node, Object data);
  public Object visit(ASTNENode node, Object data);
  public Object visit(ASTLTNode node, Object data);
  public Object visit(ASTGTNode node, Object data);
  public Object visit(ASTLENode node, Object data);
  public Object visit(ASTGENode node, Object data);
  public Object visit(ASTERNode node, Object data);
  public Object visit(ASTNRNode node, Object data);
  public Object visit(ASTAdditiveNode node, Object data);
  public Object visit(ASTAdditiveOperator node, Object data);
  public Object visit(ASTMulNode node, Object data);
  public Object visit(ASTDivNode node, Object data);
  public Object visit(ASTModNode node, Object data);
  public Object visit(ASTUnaryMinusNode node, Object data);
  public Object visit(ASTBitwiseComplNode node, Object data);
  public Object visit(ASTNotNode node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTNullLiteral node, Object data);
  public Object visit(ASTTrueNode node, Object data);
  public Object visit(ASTFalseNode node, Object data);
  public Object visit(ASTNumberLiteral node, Object data);
  public Object visit(ASTStringLiteral node, Object data);
  public Object visit(ASTArrayLiteral node, Object data);
  public Object visit(ASTMapLiteral node, Object data);
  public Object visit(ASTMapEntry node, Object data);
  public Object visit(ASTEmptyFunction node, Object data);
  public Object visit(ASTSizeFunction node, Object data);
  public Object visit(ASTFunctionNode node, Object data);
  public Object visit(ASTMethodNode node, Object data);
  public Object visit(ASTSizeMethod node, Object data);
  public Object visit(ASTConstructorNode node, Object data);
  public Object visit(ASTArrayAccess node, Object data);
  public Object visit(ASTReferenceExpression node, Object data);
}
/* JavaCC - OriginalChecksum=f2615ee9b73c6cbc34e6ef16e74f112b (do not edit this line) */
