package ece651.mini_amazon.controller;

import ece651.mini_amazon.dao.CartDao;
import ece651.mini_amazon.exceptions.serviceException.*;
import ece651.mini_amazon.exceptions.utilsException.IntFormatException;
import ece651.mini_amazon.model.Item;
import ece651.mini_amazon.service.webService.ShoppingService;
import ece651.mini_amazon.utils.tools.Logger;
import ece651.mini_amazon.dao.ProductDao;
import ece651.mini_amazon.dao.UserDao;
import ece651.mini_amazon.model.Cart;
import ece651.mini_amazon.model.Product;
import ece651.mini_amazon.model.User;
import ece651.mini_amazon.service.webService.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class HomeController {
    // declare dao
    @Autowired
    private UserDao userDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    UserService userService;
    @Autowired
    CartDao cartDao;
    @Autowired
    ShoppingService shoppingService;


    @RequestMapping("/")
    public ModelAndView home(HttpSession session) {

        ArrayList<Product> products = (ArrayList<Product>) this.productDao.findAll();
        ModelAndView mv = new ModelAndView();
        mv.setViewName("home.jsp");
        mv.addObject("products", products);
        ArrayList<String> categories = shoppingService.getCategories();
        mv.addObject("categories", shoppingService.getCategories());

        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            mv.addObject("userName", null);
            mv.addObject("userId", null);
            return mv;
        }

        mv.addObject("userName", user.getUsername());
        mv.addObject("userId", userID.toString());
        return mv;
    }

    /*
    Login to the account.
    Return to the home with userID.
     */
    @RequestMapping("/verify_login")
    public ModelAndView logging(HttpSession session,
                                @RequestParam("name") String username,
                                @RequestParam("password") String password) {

        ModelAndView mv = new ModelAndView();

        if (username.equals("") || password.equals("")) {
            return new ModelAndView("redirect:/login");
        }

        Long userID;
        try {
            userID = userService.login(username, password);
        } catch (LoginException e) {
            mv.setViewName("loginFailed.jsp");
            mv.addObject("loginFailureMsg", e.getMsg());
            return mv;
        }

        session.setAttribute("userID", userID);

        return new ModelAndView("redirect:/");
    }

    /*
    Redirect to the log in page.
     */
    @RequestMapping("/login")
    public String login() {
        return "login.jsp";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("userID");
        return "redirect:/login";
    }

    @RequestMapping(value = "/loginMust", method = RequestMethod.GET)
    public String loginWithMsg(RedirectAttributes redirectAttributes) {
        String msg = "You have to login first in order to add item in your cart.";
        redirectAttributes.addAttribute("message", msg);
        return "redirect:/login";
    }

    /*
    Redirect to the register page.
     */
    @RequestMapping("/registration")
    public String registration() {
        return "register.jsp";
    }

    /*
    Create an account and return to the home.
     */
    @RequestMapping("/register")
    public ModelAndView register(HttpSession session,
                                 @RequestParam("name") String username,
                                 @RequestParam("password1") String password1,
                                 @RequestParam("password2") String password2) {
        Long userID;
        if (username.equals("") || password1.equals("") || password2.equals("")) {
            return new ModelAndView("redirect:/registration");
        }
        try {
            userID = userService.register(username, password1, password2);
        } catch (RegisterException e) {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("loginFailed.jsp");
            mv.addObject("loginFailureMsg", e.getMsg());
            return mv;
        }
        session.setAttribute("userID", userID);  // add user ID to http session
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/cart/{userId}/{itemId}")
    public String addToCart(@PathVariable("userId") String userId, @PathVariable("itemId") String itemId, HttpSession session) {
        User user;
        Long userID = (Long)session.getAttribute("userID");
        try {
            user = userService.getUser(userID);
        }catch (NoSuchUserException e) {
            Logger.logErr(e.getMsg());
            return "redirect:/registration";
        }

        int productID = Integer.parseInt(itemId);
        Product product;
        try {
            product = shoppingService.getProduct(productID);
            shoppingService.addToCart(product, 1, user);
        } catch (NoSuchProductException e) {
            Logger.logErr(e.getMsg());
        }

        return "redirect:/";
    }


    @RequestMapping(value = "/categories/{category}")
    public ModelAndView shopCategories(HttpSession session, @PathVariable("category") String category) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("home.jsp");
        mv.addObject("categories", shoppingService.getCategories());
        ArrayList<Product> products = shoppingService.category(category);
        mv.addObject("products", products);
        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            mv.addObject("userName", null);
            mv.addObject("userId", null);
            return mv;
        }

        mv.addObject("userName", user.getUsername());
        mv.addObject("userId", userID.toString());
        return mv;
    }

    // bh214: for testing the purchaseItemsInCart method
    @RequestMapping("/testBuy")
    public String purchaseItemsInCart(HttpSession session) {
        User user;
        Long userID = (Long)session.getAttribute("userID");
        try {
            user = userService.getUser(userID);
        }catch (NoSuchUserException e) {
            Logger.logErr(e.getMsg());
            return "redirect:/registration";
        }

        try {
            this.shoppingService.purchaseItemsInCart(user, "100", "200", "Eunbi");
        } catch (NoSuchWarehouseException e) {
            Logger.logErr(e.getMsg());
        } catch (StockNotEnoughException e) {
            Logger.logMsg(e.getMsg());
        } catch (IntFormatException e) {
            Logger.logErr(e.getMsg());
        }
        return "redirect:/";
    }


    /*
    Display the Cart.
     */
    @RequestMapping(value = "/inCart")
    public ModelAndView inCart(HttpSession session) {
        Long userID = (Long)session.getAttribute("userID");
        User owner;
        try {
            owner = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            return new ModelAndView("redirect:/login");
        }

        Cart cart = owner.getCart();
        ArrayList<Item> items = new ArrayList<>();
        try {
            items = shoppingService.getItemList(new JSONObject(cart.getItems()));
        } catch (NoSuchProductException e) {
            Logger.logErr(e.getMsg());
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("cart.jsp");
        mv.addObject("userId", userID.toString());
        mv.addObject("userName", owner.getUsername());
        mv.addObject("items", items);
        mv.addObject("total", shoppingService.getTotalPrice(items));
        return mv;
    }

    //formal purchase
    @RequestMapping(value="/purchase/{positionX}/{positionY}/{upsAccount}")
    public ModelAndView purchase(@PathVariable("positionX") String posX, @PathVariable("positionY") String posY, @PathVariable("upsAccount") String upsAccount, HttpSession session) {
        ModelAndView mv = new ModelAndView();

        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            return new ModelAndView("redirect:/login");
        }


        Cart cart = user.getCart();
        ArrayList<Item> items = new ArrayList<>();
        try {
            items = shoppingService.getItemList(new JSONObject(cart.getItems()));
        } catch (NoSuchProductException e) {
            Logger.logErr(e.getMsg());
        }

        String account = "";
        if (upsAccount != null && !upsAccount.equals("null")) {
            account = upsAccount;
        }
        int tracking=0;
        try {
            tracking = shoppingService.purchaseItemsInCart(user, posX, posY, account);
        } catch (StockNotEnoughException e) {
            mv.setViewName("confirmation.jsp");
            mv.addObject("message", e.getMsg());
            return mv;
        } catch (NoSuchWarehouseException e) {
            Logger.logErr(e.getMsg());
        } catch (IntFormatException e) {
            return new ModelAndView("redirect:/inCart");
        }

        mv.setViewName("confirmation.jsp");
        mv.addObject("userId", userID.toString());
        mv.addObject("userName", user.getUsername());

        //redirect success, and tracking number
        mv.addObject("tracking", tracking);

        mv.addObject("items", items);
        mv.addObject("total", shoppingService.getTotalPrice(items));
        return mv;
    }


    @RequestMapping("/search")
    public ModelAndView checkCart(HttpSession session) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("search.jsp");
        mv.addObject("products", new ArrayList<>());

        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            mv.addObject("userName", null);
            mv.addObject("userId", null);
            return mv;
        }

        mv.addObject("userName", user.getUsername());
        mv.addObject("userId", userID.toString());
        return mv;
    }

    @RequestMapping("/searching")
    public ModelAndView searching(HttpSession session, @RequestParam("searchInfo") String searchInfo) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("search.jsp");

        ArrayList<Product> results = shoppingService.search(searchInfo);
        mv.addObject("products", results);

        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            mv.addObject("userName", null);
            mv.addObject("userId", null);
            return mv;
        }

        mv.addObject("userName", user.getUsername());
        mv.addObject("userId", userID.toString());

        return mv;
    }

    @RequestMapping("/trackOrder")
    public ModelAndView tracOrder(HttpSession session) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("tracking.jsp");
        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            return new ModelAndView("redirect:/login");
        }
        mv.addObject("userId", userID.toString());
        mv.addObject("userName", user.getUsername());
        return mv;
    }

    @RequestMapping("/tracking")
    public ModelAndView tracking(HttpSession session, @RequestParam("orderId") String id) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("tracking.jsp");
        Long userID = (Long)session.getAttribute("userID");
        User user;
        try {
            user = userService.getUser(userID);
        } catch (NoSuchUserException e) {
            return new ModelAndView("redirect:/login");
        }
        mv.addObject("userId", userID.toString());
        mv.addObject("userName", user.getUsername());

        ece651.mini_amazon.model.Package p;
        try {
            p = userService.trackingPackage(user, id);
        } catch (NoSuchPackageException e) {
            mv.addObject("message", e.getMsg());
            return mv;
        } catch (IntFormatException e) {
            mv.addObject("message", e.getMsg());
            return mv;
        }
        ArrayList<Item> items = new ArrayList<>();
        try {
            items = shoppingService.getItemList(new JSONObject(p.getProducts()));
        }catch (NoSuchProductException e) {
            Logger.logErr(e.getMsg());
        }
        mv.addObject("items", items);
        mv.addObject("thePackage", p);
        mv.addObject("total", shoppingService.getTotalPrice(items));
        return mv;
    }
}
