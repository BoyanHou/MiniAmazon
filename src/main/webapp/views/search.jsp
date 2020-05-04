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

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="author" content="colorlib.com">
    <link href="https://fonts.googleapis.com/css?family=Poppins:400,800" rel="stylesheet" />
    <link href="${pageContext.request.contextPath}/css/main.css" rel="stylesheet" />

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


<!--================Searching Area =================-->

<section class="banner_area">
<div class="s004">
    <form action="searching">
        <h4>WHAT ARE YOU LOOKING FOR?</h4>
        <div class="inner-form">
            <div class="input-field">
                <input class="form-control" id="searchInfo" name="searchInfo" type="text" placeholder="Type to search..." />
                <button class="btn-search" type="submit">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
                    </svg>
                </button>
            </div>
        </div>
    </form>
</div>
</section>

<!--================Category Product Area =================-->
<section class="feature_product_area">
    <div class="main_box">
        <div class="container-fluid">
            <div class="row">
                <div class="main_title">
                    <h2>Search Result</h2>
                </div>
            </div>
            <c:if test="${empty products}">
                <div class="container">
                    <h4 class="footer_title">No Matching Result</h4>
                </div>
            </c:if>
            <div class="row">
                <c:if test="${not empty products}">
                    <div class="latest_product_inner row">
                        <c:forEach var="product" items="${products}">
                            <div class="col-lg-3 col-md-3 col-sm-6">
                                <div class="f_p_item">
                                    <div class="f_p_img">
                                        <img class="img-fluid" src="../img/${product.photo}" alt="">
                                        <div class="p_icon">
                                            <c:if test="${not empty userId}">
                                                <a href="http://localhost:7070/cart/${userId}/${product.id}">
                                                    <i class="lnr lnr-cart"></i>
                                                </a>
                                            </c:if>

                                            <c:if test="${empty userId}">
                                                <a href="http://localhost:7070/login">
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
                </c:if>

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

</html></html>