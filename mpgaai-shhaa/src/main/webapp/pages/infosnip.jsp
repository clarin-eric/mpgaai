<%@page import="de.mpg.aai.shhaa.AAIServletRequest, de.mpg.aai.shhaa.context.*,de.mpg.aai.shhaa.model.*" %>

<%
	boolean aaiOn = false;
	AAIServletRequest aaiReq = null;
	AuthenticationContext aaiCtx = null;
	if(request instanceof AAIServletRequest) {
		aaiOn = true;
		aaiReq = (AAIServletRequest) request;
		aaiCtx = aaiReq.getAuthenticationContext();
	}
%>

<!--** USERNAME info *******************************************************-->
<div class="table">
	<table>
		<tr><th colspan="2">Your Current Login</th></tr>
		<tr><td>User-Name</td><td><b><%= request.getRemoteUser() %></b></td></tr>
	</table>
</div>


<%	if(aaiOn) {	/* /display aai infos */ %>


<!--** ATTRIBUTES info *****************************************************-->
<%
	AuthAttributes attributes = aaiCtx.getAuthAttributes();
	java.util.Set<String> ids = attributes.getIDs();
%>
<div class="table">
	<table>
	<tr><th colspan="2">Your Attributes</th></tr>
		<tr><td>count</td><td><%= attributes.size() %></td></tr>
<%
		for(String id : ids ) {
			AuthAttribute<?> attb = attributes.get(id);
			java.util.Set<?> values = attb.getValues();
			int ii=0;
			for(Object val : values)  {
				if(ii++==0) {
%>		
					<tr><td rowspan="<%= values.size() %>"><%= attb.getID() %></td><td>&nbsp;<%= val.toString() %></td></tr>
<%				} else {	%>
					<tr><td>&nbsp;<%= val.toString() %></td></tr>
<%				}
			}
		}	
%>
	</table>
</div>


<!--** SESSION info ********************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">Current Session</th></tr>
		<tr><td>Session-ID</td><td><%= aaiCtx.getSessionID() %></td></tr>
		<tr><td>Identity Provider</td><td><%= aaiCtx.getIdentiyProviderID() %></td></tr>
		<tr><td>Login Time</td><td><%= aaiCtx.getLoginTime() %></td></tr>
	</table>
</div>

<% }	/* /display aai infos */ %>
