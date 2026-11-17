<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" >
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.Enumeration" %>
<html>
<head>
	<title>request info</title>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" >
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/shhaa.css" >
</head>

<body><div class="body" >
	<div class="title" >SHHAA-Filter</div>
	<div class="subtitle" >Simple Http Header Authentication &amp; Authorization</div>


<%
	Enumeration<?> items = null;
	Object		item = null; 
%>


<div class="heading">List Request &amp; Session Data:</div>


<!--** REQUEST HEADER ******************************************************-->
<div>
	<table>
	<tr><th colspan="2">request header</th></tr>
	<%
		items= request.getHeaderNames();
		while(items.hasMoreElements()) {
			item = items.nextElement();
	%>
		<tr><td><%= item %></td><td><%= request.getHeader(item.toString()) %></td></tr>
	<%
		}
	%>
	</table>
</div>



<!--** REQUEST ATTRIBUTES ***************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">request attributes</th></tr>
	<%
		items= request.getAttributeNames();
		while(items.hasMoreElements()) {
			item = items.nextElement();
	%>
		<tr><td><%= item %></td><td><%= request.getAttribute(item.toString()) %></td></tr>
	<%
		}
	%>
	</table>
</div>



<!--** REQUEST PARAMETER ***************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">request parameter</th></tr>
	<%
		items= request.getParameterNames();
		while(items.hasMoreElements()) {
			item = items.nextElement();
	%>
		<tr><td><%= item %></td><td><%= request.getParameter(item.toString()) %></td></tr>
	<%
		}
	%>
	</table>
</div>



<!--** SESSION ATTRIBUTES **************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">session attributes</th></tr>
	<%
		items= session.getAttributeNames();
		while(items.hasMoreElements()) {
			item = items.nextElement();
	%>
		<tr><td><%= item %></td><td><%= session.getAttribute(item.toString()) %></td></tr>
	<%
		}
	%>
	</table>
</div>



<!--** COOKIES *************************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">cookies</th></tr>
	<%
		Cookie[] cookies = request.getCookies();
		for(int ii=0 ; ii < cookies.length ; ii++) {
			Cookie cookie = cookies[ii];
	%>
		<tr><td><%= cookie.getName() %></td><td><%= cookie.getValue() %></td></tr>
	<%
		}
	%>
	</table>
</div>


<!--** USER INFO ***********************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">user info</th></tr>
		<tr><td>remote user</td><td><%= request.getRemoteUser() %></td></tr>
		<tr><td>user principal</td><td><%= request.getUserPrincipal() %></td></tr>
		<tr><td>REMOTE_USER</td><td><%= request.getHeader("REMOTE_USER") %></td></tr>
		<tr><td>request class</td><td><%= request.getClass().getName() %></td></tr>
		<tr><td>auth type</td><td><%= request.getAuthType() %></td></tr>
	 </table>
</div>


<!--** REQUEST INFO ********************************************************-->
<div class="table">
	<table>
	<tr><th colspan="2">request info</th></tr>
		<tr><td>protocol</td><td><%= request.getProtocol() %></td></tr>
		<tr><td>secure</td><td><%= request.isSecure() %></td></tr>
		<tr><td>scheme</td><td><%= request.getScheme() %></td></tr>
		<tr><td>local addr</td><td><%= request.getLocalAddr() %></td></tr>
		<tr><td>local port</td><td><%= request.getLocalPort() %></td></tr>
		<tr><td>server port</td><td><%= request.getServerPort() %></td></tr>
		<tr><td>local name</td><td><%= request.getLocalName() %></td></tr>
		<tr><td>server name</td><td><%= request.getServerName() %></td></tr>
		<tr><td>context path</td><td><%= request.getContextPath() %></td></tr>
		<tr><td>servlet path</td><td><%= request.getServletPath() %></td></tr>
		<tr><td>request url</td><td><%= request.getRequestURL() %></td></tr>
		<tr><td>request uri</td><td><%= request.getRequestURI() %></td></tr>
		<tr><td>path info</td><td><%= request.getPathInfo() %></td></tr>
		<tr><td>path translated</td><td><%= request.getPathTranslated() %></td></tr>
		<tr><td>query string</td><td><%= request.getQueryString() %></td></tr>
		<tr><td>method</td><td><%= request.getMethod() %></td></tr>
 		<tr><td>locale</td><td><%= request.getLocale() %></td></tr>
 		<tr><td>content type</td><td><%= request.getContentType() %></td></tr>
 		<tr><td>character encoding</td><td><%= request.getCharacterEncoding() %></td></tr>

		<tr><td>remote host</td><td><%= request.getRemoteHost() %></td></tr>
		<tr><td>remote addr</td><td><%= request.getRemoteAddr() %></td></tr>
 		<tr><td>remote port</td><td><%= request.getRemotePort() %></td></tr>
</table>
</div>


</div></body>
</html>
