<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="xc" %>

<!DOCTYPE html>
<html lang="en">
<head>
<title>Societies</title>
<meta charset="utf-8">
<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="css/dialog.css">
<script>!window.jQuery && document.write('<script src="js/jquery-v1.8.1.js"><\/script>')</script>
<!-- Menu -->
<script src="js/webmenu_nav.js"></script>
<!--[if IE]>
<script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]--> 
<!--[if lt IE 7]>
<link rel="stylesheet" type="text/css" media="all" href="css/ie7.css" />
<script src="http://ie7-js.googlecode.com/svn/version/2.1(beta4)/IE7.js"></script><![endif]-->
<!--[if IE 7]>
<link rel="stylesheet" type="text/css" media="all" href="css/ie7.css" /><![endif]-->
<!--script for PopUp for Login/Register--> 
<script src="js/webpopup.js"></script>
</head>
<body>
<div id="wrapper" class="clearfix">
<div id="container" class="container_12 clearfix">
<!-- Header -->
<header id="header" class="grid_12">
<div class="login-form">
<a href="#login-box" class="login-window">LOGIN / REGISTER</a>
</div>
<div id="login-box" class="login-popup">
<a href="#" class="close"><img src="images/close_pop.png" class="btn_close" title="Close Window" alt="Close" /></a>
<form method="post" class="signin" action="#">
<fieldset class="textbox">
<label class="username">
<span>Username</span>
<input id="username" name="username" value="" type="text" autocomplete="on" placeholder="Username">
</label>
<label class="password">
<span>Password</span>
<input id="password" name="password" value="" type="password" placeholder="Password">
</label>
<button class="submit button" type="button">Sign in</button>
<p>Don't have a login?
<!-- TODO: Hardcoded for now -->
<a class="" href="http://localhost:8080/domain-authority/">Sign Up</a>
</p>        
</fieldset>
</form>
</div>
<!-- Logo -->
<h1 id="logo" class="logo-pos">
<!-- TODO: Hardcoded for now -->
<a href="http://localhost:8080/domain-authority/"><img src="images/societies_logo.jpg" alt="Logo" /></a>
</h1>
<!--WebMenu -->
<nav id="webmenu_nav">
<ul id="navigation" class="grid_8">
<li><a href="myprofile.html"><br />My Account</a>
<ul class="sub-menu"> 
<li><a href="myprofile.html">My Profile</a></li>
<li><a href="editprofile.html">Edit Settings</a></li>
<!-- <li><a href="settings.html">Security Settings</a></li>
<li><a href="privacysettings.html">Privacy Settings</a></li> -->
</ul>
</li>
<!-- 
<li><a href="your_installed_apps.html"><br />Apps</a>
</li>
<li><a href="your_societies_friends_list.html"><br />Friends</a>
<ul class="sub-menu"> 
<li><a href="your_societies_friends_list.html">Your Friends</a></li>
<li><a href="suggested_societies_friends_list.html">Suggested Friends</a></li>
</ul>
</li>
<li><a href="your_communities_list.html"><br />Communities</a>
<ul class="sub-menu"> 
<li><a href="your_communities_list.html">Your Communities</a></li>
<li><a href="manage_communities.html">Manage your Communities</a></li>
<li><a href="create_community.html">Create a Community</a></li>
<li><a href="your_suggested_communities_list.html">Suggested Communities</a></li>
</ul>
</li>
-->
<li><a href="main.html" class="current"><br />Home</a></li>
</ul>
</nav><!-- #webmenu -->
<div class="clear"></div>
</header>
<!--Main Content -->
<section id="featured" class="clearfix grid_12">
<div>
<article> 
</article>			
</div>
</section>
<div class="hr grid_12 clearfix boxhr">&nbsp;</div>
<section class="homebox_entries grid_12">
<!-- FIRST ROW -->
<header>
  <h4 class="box_title">NETWORKING ZONE </h4></header>
<div class="hr grid_12 clearfix">&nbsp;</div>
<article>
<a class="homeboxgrid box " href="your_societies_friends_list.html" title="Zone A">
<img src="images/zone.png" width="270" height="150" alt="Friends"></a>
</article>
<article>
<a class="homeboxgrid box " href="your_installed_apps.html" title="Zone B">
<img src="images/zone.png" width="270" height="150" alt="Apps"> </a>
</article>
<article>
<a class="homeboxgrid box " href="your_communities_list.html" title="Zone C">
<img src="images/zone.png" width="270" height="150" alt="Communities"> </a>
</article>
</section>
<div class="hr grid_12 clearfix">&nbsp;</div>
</div>
<!-- Footer -->
<footer class="container_12 clearfix">
<section class="footer">
<p class="footer-links">
<span><a href="termsofuse.html">Terms of Use</a> | <a href="disclaimer.html">Disclaimer</a> | <a href="privacy.html">Privacy</a> | <a href="help.html">Help</a> | <a href="about.html">About</a></span>
<a class="float right toplink" href="#">top</a>
</p>
</section>
</footer>
</div>
</body>
</html>