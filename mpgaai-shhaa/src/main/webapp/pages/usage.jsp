<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" >
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="de.mpg.aai.shhaa.AAIServletRequest, de.mpg.aai.shhaa.context.*,de.mpg.aai.shhaa.model.*, de.mpg.aai.security.auth.model.*" %>
<html>
<head>
	<title>usage how-to</title>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" >
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/shhaa.css" >
</head>
<body><div class="body" >

	<div class="title" >SHHAA-Filter</div>
	<div class="subtitle" >Simple Http Header Authentication &amp; Authorization</div>
	
	<div class="heading">Filter Usage Samples</div>
	<p>short documentation by sample how-to get session- &amp; user-information from the filter</p>

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


<!--** AAI-ServletRequest INFO **********************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">SHHAA Request</th></tr>
		<tr><td>request.class.name</td><td><%= request.getClass().getName() %></td></tr>
		<tr><td>request.userPrincipal</td><td><%= request.getUserPrincipal() %></td></tr>
		<tr><td>request.remoteUser</td><td><%= request.getRemoteUser() %></td></tr>
	</table>
</div>


<%	if(aaiOn) {	/* /display aai infos */ %>

<!--** Authentication CONTEXT info *****************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">Authentication Context</th></tr>
		<tr><td>request.authenticationContext</td><td><%= aaiReq.getAuthenticationContext() %></td></tr>
		<tr><td>aaiCtx.sessionID</td><td><%= aaiCtx.getSessionID() %></td></tr>
		<tr><td>aaiCtx.identityProviderID</td><td><%= aaiCtx.getIdentiyProviderID() %></td></tr>
		<tr><td>aaiCtx.loginTime</td><td><%= aaiCtx.getLoginTime() %></td></tr>
		<tr><td>aaiCtx.readOnly</td><td><%= aaiCtx.isReadOnly() %></td></tr>
		<tr><td>aaiCtx.subject.class.name</td><td><%= aaiCtx.getSubject().getClass().getName() %></td></tr>
		<tr><td>aaiCtx.subject</td><td><%= aaiCtx.getSubject() %></td></tr>
		<tr><td>aaiCtx.subject.readOnly</td><td><%= aaiCtx.getSubject().isReadOnly() %></td></tr>
		<tr><td>aaiCtx.subject.principals</td><td><%= aaiCtx.getSubject().getPrincipals() %></td></tr>
		<tr><td>aaiCtx.authPrincipal.class.name</td><td><%= aaiCtx.getAuthPrincipal().getClass().getName() %></td></tr>
		<tr><td>aaiCtx.aaiPrincipal.name</td><td><%= aaiCtx.getAuthPrincipal().getName() %></td></tr>
		<tr><td>aaiCtx.aaiPrincipal.readOnly</td><td><%= aaiCtx.getAuthPrincipal().isReadOnly() %></td></tr>
	</table>
</div>


<!--** authCtx - ATTRIBUTES info *******************************************-->
<%
	AuthAttributes attributes = aaiCtx.getAuthAttributes();
	java.util.Set<String> ids = attributes.getIDs();
%>
<div class="table">
	<table>
	<tr><th colspan="2">authCtx - attributes</th></tr>
		<tr><td>aaiCtx.aaiAttributes</td><td><%= attributes %></td></tr>
		<tr><td>aaiCtx.aaiPrincipal.attributes</td><td><%= aaiCtx.getAuthPrincipal().getAttribues() %></td></tr>
		<tr><td>attributes.size</td><td><%= attributes.size() %></td></tr>
		<tr><td>attributes.readOnly</td><td><%= attributes.isReadOnly() %></td></tr>
<%
		for(String id : ids ) {
			AuthAttribute<?> attb = attributes.get(id);
			java.util.Set<?> values = attb.getValues();
			int ii=0;
			for(Object val : values)  {
				if(ii++==0) {
%>		
					<tr>
						<td rowspan="<%= values.size() %>"><%= attb.getID() %> <div class="ro">(readOnly: <%= attb.isReadOnly() %>)</div></td>
						<td>&nbsp;<%= val.toString() %></td></tr>
<%				} else {	%>
					<tr><td>&nbsp;<%= val.toString() %></td></tr>
<%				}
			}
		}	
%>
	</table>
</div>


<!--** authContext retrieval  **********************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">Authentication Context - Retrieval</th></tr>
		<tr><td>request.authenticationContext</td><td>(hashcode) <%= aaiReq.getAuthenticationContext().hashCode() %></td></tr>
		<tr><td>AuthenticationContextHolder.get(request)</td><td>(hashcode) <%= AuthenticationContextHolder.get(request).hashCode() %></td></tr>
		<tr><td>AuthenticationContextHolder.get(session)</td><td>(hashcode) <%= AuthenticationContextHolder.get(request.getSession()).hashCode() %></td></tr>
		<tr><td>AuthenticationContextHolder.get() [from ThreadLocal]</td><td>(hashcode) <%= AuthenticationContextHolder.get().hashCode() %></td></tr>
	</table>
</div>

<% }	/* /display aai infos */ %>

</div></body>
</html>
