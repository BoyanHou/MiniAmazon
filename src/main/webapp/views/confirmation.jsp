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
						<a href="/logout">Logout</a>
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
				<h2>Order Confirmation</h2>
			</div>
		</div>
	</div>
</section>
<!--================End Home Banner Area =================-->

	<!--================Order Details Area =================-->
	<section class="order_details p_120">
		<div class="container">
			<h3 class="title_confirmation">${message}</h3>
			<c:if test="${not empty tracking}">
			<div class="row order_d_inner">
				<div class="col-lg-4">
					<div class="details_item">
						<h4>Order Info</h4>
						<ul class="list">
							<li>
								<a href="#">
									<span>tracking number</span> : ${tracking}</a>
							</li>

							<li>
								<a href="#">
									<span>Total</span> : ${total}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
			<div class="order_details_table">
				<h2>Order Details</h2>
				<div class="table-responsive">
					<table class="table">
						<thead>
							<tr>
								<th scope="col">Product</th>
								<th scope="col">Quantity</th>
								<th scope="col">Total</th>
							</tr>
						</thead>
						<tbody>
						<c:forEach var="item" items="${items }">
							<tr>
								<td>
									<div class="media">
										<div class="d-flex">
											<img src="${pageContext.request.contextPath}/smallImg/${item.product.photo}" alt="">
										</div>
										<div class="media-body">
											<p>${item.product.description}</p>
										</div>
									</div>
								</td>

								<td>
									<h5>$${item.product.price}</h5>
								</td>


								<td>
									<h5>${item.quantity}</h5>
								</td>


								<td>
									<h5>
										<script>
											var total = ${item.product.price} * ${item.quantity}
											document.write(total)
										</script>
									</h5>
								</td>
							</tr>
						</c:forEach>
							<tr>
								<td>
									<h4>Total</h4>
								</td>
								<td>
									<h5></h5>
								</td>
								<td>
									<p>${total}</p>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			</c:if>
		</div>
	</section>
	<!--================End Order Details Area =================-->

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