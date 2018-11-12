<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" >
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>SHAAA-Filter</title>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" >
	<link rel="stylesheet" type="text/css" href="css/shhaa.css" >
</head>

<body><div class="body" >

	<div class="title" >SHHAA-Filter</div>
	<div class="subtitle" >Simple Http Header Authentication &amp; Authorization</div>
	<br />
	<div class="title" >-= Demo Web App =-</div>
	<br />
	<br />
	<div class="table">
		<table>
			<tr><th colspan="2">Tests - try how it works</th></tr>
			<tr><td><a href="pages/protected/sample.jsp">protected site</a></td><td>access to protected site, <br />invokes sso login if not authenticated yet and auto-login is configured (default)</td></tr>
			<tr><td><a href="?shhaaDo=lI">login</a></td><td>calls Single-Sign-On</td></tr>
			<tr><td><a href="?shhaaDo=lO">logout</a></td><td>calls Single-Log-Out</td></tr>
			<tr><td><a href="?shhaaDo=dI">show session/login info</a></td><td>forces display of session/login info page,<br />(if <code>/shhaa/handler/page/info</code> is configured)</td></tr>
			<tr><td><a href="?shhaaDo=dE">show expired notification</a></td><td>forces display of expired page,<br />(if <code>/shhaa/handler/page/expired</code> is configured)</td></tr>
			<tr><td><a href="?shhaaDo=dD">show access-denied notification</a></td><td>forces display of access-denied page,<br />(if <code>/shhaa/handler/page/denied</code> is configured)</td></tr>
			<tr><td><a href="?shhaaDo=rF">refresh attributes</a></td><td>re-runs the attribute lookup for the current user<br />(if <code>/shhaa/composition.action</code> is configured)</td></tr>
		</table>
	</div>

	<div class="table">
		<table>
			<tr><th>more</th></tr>
			<tr><td><a href="pages/request.jsp">displays request data</a></td></tr>
			<tr><td><a href="pages/usage.jsp">usage: sample page how-to get data from the filter</a></td></tr>
			<tr><td><a href="site/index.html">documentation &amp; howto</a></td></tr>
		</table>
	</div>

</div></body>
</html>
