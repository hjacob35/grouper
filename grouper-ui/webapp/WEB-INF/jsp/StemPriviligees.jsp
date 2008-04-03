<%-- @annotation@ 
			Displays list of Subjects with selected privilege, with links 
			to edit privileges for individual Subjects
--%><%--
  @author Gary Brown.
  @version $Id: StemPriviligees.jsp,v 1.6 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insert definition="showStemsLocationDef"/>
<tiles:insert definition="selectStemPrivilegeDef"/>
<grouper:subtitle key="stems.heading.list-members" />

<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="privilegeLinks"/>
	<tiles:put name="headerView" value="privilegeLinksHeader"/>
	<tiles:put name="itemView" value="privilegeLink"/>
	<tiles:put name="footerView" value="privilegeLinksFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="noResultsMsg" value="${navMap['stems.list-privilegees.none']}"/>
	<tiles:put name="listInstruction" value="list.instructions.privilege-links"/> 
	<tiles:put name="linkSeparator">  
		<tiles:insert definition="linkSeparatorDef" flush="false"/>
	</tiles:put>
</tiles:insert>
<div class="linkButton">
<%--<c:if test="${!empty searchObj && searchObj.trueSearch}">
	<c:set target="${searchObj}" property="stems" value="true"/>
	<html:link page="/searchNewMembers.do" name="searchObj">
		<grouper:message bundle="${nav}" key="find.stems.membersreturn-results"/>
	</html:link>
</c:if>--%>

<html:link page="/populateFindNewMembersForStems.do" name="stemMembership">
	<grouper:message bundle="${nav}" key="find.stems.add-new-privilegees"/>
</html:link>
<html:link page="/populate${browseMode}Groups.do" >
	<grouper:message bundle="${nav}" key="priv.stems.list.cancel"/>
</html:link>
<c:if test="${isNewStem && !empty findForNode}">
<html:link page="/populate${browseMode}Groups.do" paramId="currentNode" paramName="findForNode">
	<grouper:message bundle="${nav}" key="priv.stems.list.cancel-and-work-in-new"/>
</html:link>
</c:if>
</div>
