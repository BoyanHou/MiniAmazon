<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">

<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<title>私厨305</title>
	<!-- Bootstrap CSS -->
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vendors/linericon/style.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/font-awesome.min.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vendors/owl-carousel/owl.carousel.min.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vendors/lightbox/simpleLightbox.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vendors/nice-select/css/nice-select.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vendors/animate-css/animate.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vendors/jquery-ui/jquery-ui.css">
	<!-- main css -->
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>


<body>

<!--================Header Menu Area =================-->
<header class="header_area">
	<div class="top_menu row m0">
		<div class="container-fluid">
			<div class="float-left">
				<p>Call Us: 666-666-6666</p>
			</div>
			<div class="float-right">
				<ul class="right_side">
					<li>
					<c:if test="${not empty userId}">
						Hello ${userName}!
					</c:if>
					<c:if test="${empty userId}">
						<a href="/login">
							Login/Register
						</a>
					</c:if>
					</li>
					<li>
						<a href="/trackOrder">
							Tracking Orders
						</a>
					</li>
					<li>
						<a href="/logout">
						Logout
						</a>
					</li>
				</ul>
			</div>
		</div>
	</div>
	<div class="main_menu">
		<nav class="navbar navbar-expand-lg navbar-light">
			<div class="container-fluid">
				<!-- Brand and toggle get grouped for better mobile display -->
				<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
						aria-expanded="false" aria-label="Toggle navigation">
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<div class="collapse navbar-collapse offset" id="navbarSupportedContent">
					<div class="row w-100">
						<div class="col-lg-7 pr-0">
							<ul class="nav navbar-nav center_nav pull-right">
								<li class="nav-item active">
									<a class="nav-link" href="/">HOME</a>
								</li>

								<%--									<li class="nav-item">--%>
								<%--										<a class="nav-link" href="/shop">SHOP</a>--%>
								<%--									</li>--%>
							</ul>
						</div>
						<div class="col-lg-5">
							<ul class="nav navbar-nav navbar-right right_nav pull-right">
								<hr>
								<li class="nav-item">
									<a href="/search" class="icons">
										<i class="fa fa-search" aria-hidden="true"></i>
									</a>
								</li>

								<hr>

								<c:if test="${not empty userId}">
									<li class="nav-item">
										<a href="/inCart" class="icons">
											<i class="lnr lnr lnr-cart"></i>
										</a>
									</li>
								</c:if>

								<c:if test="${empty userId}">
									<li class="nav-item">
										<a href="/login" class="icons">
											<i class="lnr lnr lnr-cart"></i>
										</a>
									</li>
								</c:if>

								<hr>
							</ul>
						</div>
					</div>
				</div>
			</div>
		</nav>
	</div>
</header>
<!--================Header Menu Area =================-->

	<!--================Home Banner Area =================-->
	<section class="banner_area">
		<div class="banner_inner d-flex align-items-center" style="background-image: url('${pageContext.request.contextPath}/img/banner.jpg'); opacity: 70%">
			<div class="container">
				<div class="banner_content text-center">
					<h2>Shop Home</h2>
				</div>
			</div>
		</div>
	</section>
	<!--================End Home Banner Area =================-->

	<!--================Category Product Area =================-->
	<section class="cat_product_area section_gap">
		<div class="container-fluid">
			<div class="row flex-row-reverse">
				<div class="col-lg-9">
					<div class="product_top_bar">
					</div>
					<div class="latest_product_inner row">
						<c:forEach var="product" items="${products}">
							<div class="col-lg-3 col-md-3 col-sm-6">
								<div class="f_p_item">
									<div class="f_p_img">
										<img class="img-fluid" src="${pageContext.request.contextPath}/img/${product.photo}" alt="">
										<div class="p_icon">
											<c:if test="${not empty userId}">
											<a href="/cart/${userId}/${product.id}">
												<i class="lnr lnr-cart"></i>
											</a>
											</c:if>

											<c:if test="${empty userId}">
												<a href="/loginMust">
													<i class="lnr lnr-cart"></i>
												</a>
											</c:if>

										</div>
									</div>
									<h4>${product.description}</h4>
									<h5>$${product.price}</h5>
								</div>
							</div>
						</c:forEach>
					</div>
				</div>
				<div class="col-lg-3">
					<div class="left_sidebar_area">
						<aside class="left_widgets p_filter_widgets">
							<div class="l_w_title">
								<h3>Product Filters</h3>
							</div>
							<div class="widgets_inner">
								<h4>Categories</h4>
								<ul class="list">
									<c:forEach var="category" items="${categories}">
									<li>
										<a href="/categories/${category}">${category}</a>
									</li>
									</c:forEach>

								</ul>
							</div>
<%--							<div class="widgets_inner">--%>
<%--								<h4>Price</h4>--%>
<%--								<form action="searchPrice"></form>--%>
<%--								<div class="range_item">--%>
<%--									<div id="slider-range"></div>--%>
<%--									<div class="row m0">--%>
<%--										<label for="amount">Price : </label>--%>
<%--										<input type="text" id="amount" readonly>--%>
<%--									</div>--%>
<%--								</div>--%>
<%--								</form>--%>
<%--							</div>--%>
						</aside>
					</div>
				</div>
			</div>

		</div>
	</section>
	<!--================End Category Product Area =================-->

<!--================ start footer Area  =================-->
<footer class="footer-area section_gap">
	<div class="container">
		<h6 class="footer_title">About Us</h6>
		<p>Enjoy life. Enjoy eating.</p>
	</div>
</footer>
<!--================ End footer Area  =================-->
</body>

</html>