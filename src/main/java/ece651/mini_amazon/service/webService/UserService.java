package ece651.mini_amazon.service.webService;

import ece651.mini_amazon.dao.CartDao;
import ece651.mini_amazon.dao.PackageDao;
import ece651.mini_amazon.dao.UserDao;
import ece651.mini_amazon.exceptions.serviceException.LoginException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchPackageException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchUserException;
import ece651.mini_amazon.exceptions.serviceException.RegisterException;
import ece651.mini_amazon.exceptions.utilsException.IntFormatException;
import ece651.mini_amazon.model.Cart;
import ece651.mini_amazon.model.User;
import ece651.mini_amazon.model.Package;
import ece651.mini_amazon.utils.tools.NumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    public static final String KEY_MD5 = "MD5";

    // declare dao
    @Autowired
    private UserDao userDao;
    @Autowired
    private CartDao cartDao;
    @Autowired
    private PackageDao packageDao;

    public User getUser(Long userID) throws NoSuchUserException {
        if (userID == null) {
            throw new NoSuchUserException("No UserID Provided!");
        }
        User user = userDao.findById(userID).orElse(null);
        if (user == null) {
            throw new NoSuchUserException("No Such UserID: " + userID);
        }
        return user;
    }

    public Long login(String username, String passwordPlain) throws LoginException {
        List<User> userList =  userDao.findByUsername(username);

        String password = this.md5Encrypt(passwordPlain);

        // username does not exist
        if (userList == null || userList.size() == 0) {
            String msg = "The username:" + username + " does not exist!";
            throw new LoginException(msg);
        }

        // password incorrect
        if (!userList.get(0).getPassword().equals(password)) {
            String msg = "The password for username:" + username + " is incorrect!";
            throw new LoginException(msg);
        }

        return userList.get(0).getUserID();
    }

    public Long register(String username, String password1, String password2) throws RegisterException {

        // username is empty
        if (username.length() == 0) {
            String msg = "username cannot be empty!";
            throw new RegisterException(msg);
        }
        //password is empty
        if (password1.length() == 0 || password2.length() == 0) {
            String msg = "password cannot be empty!";
            throw new RegisterException(msg);
        }

        //password not consistent
        if (!password1.equals(password2)) {
            String msg = "Passwords should be consistent!";
            throw new RegisterException(msg);
        }

        String password = this.md5Encrypt(password1);


        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);

        // create an empty cart(package) for this user
        Cart cart = new Cart();

        cart.setOwner(newUser);
        newUser.setCart(cart);

        cartDao.save(cart);
        userDao.save(newUser);

        Long userID = userDao.findByUsername(username).get(0).getUserID();

        return userID;
    }

    public Cart createCartForUser (User user) {
        if (user.getCart() != null) {
            return user.getCart();
        }
        Cart cart = new Cart();
        cart.setOwner(user);
        user.setCart(cart);
        cartDao.save(cart);
        userDao.save(user);
        return cart;
    }

    public Package trackingPackage(User user, String idStr) throws NoSuchPackageException, IntFormatException {
        if (idStr.equals("")) {
            throw new NoSuchPackageException("Searching Id can't be empty.");
        }
        int id = NumUtils.strToInt(idStr);
        ArrayList<Package> packages = new ArrayList<>(user.getPackages());
        for (Package p: packages) {
            if (p.getShipid() == id) {
                return p;
            }
        }
        throw new NoSuchPackageException("No Such Package.");
    }

    public String md5Encrypt(String plainStr) {
        if (plainStr.length() == 0) {
            return "";
        }
        BigInteger bigInteger = null;
        // encrypt plain password to md-5 password
        try {
            MessageDigest md = MessageDigest.getInstance(KEY_MD5);
            byte[] inputData = plainStr.getBytes();
            md.update(inputData);
            bigInteger = new BigInteger(md.digest());
        } catch (Exception e) {e.printStackTrace();}

        return bigInteger.toString(16);
    }


}
