<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" >
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="de.mpg.aai.shhaa.AAIServletRequest, de.mpg.aai.shhaa.context.*,de.mpg.aai.shhaa.model.*" %>
<html>
<head>
	<title>no access</title>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" >
	<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/shhaa.css" >
</head>

<body><div class="body" >

	<div class="title" >SHHAA-Filter</div>
	<div class="subtitle" >Simple Http Header Authentication &amp; Authorization</div>
	
	
	<div class="heading">Sorry - Access Denied!</div>
	
	
	<div class="salut">Dear User,</div>
	<div class="msg" style="color: #ff0000; font-weight: bold;">you have requested a protected resource you have no access to.</div>
	<div class="msg">Please contact your system administrator in case, to endow you with the proper access-privileges.</div>
	
	
	<br /><br />
	<div class="msg">Below you can find information about your current Login- &amp; Session status:</div>
	<%@include file="infosnip.jsp" %> 

</div></body>
</html>
