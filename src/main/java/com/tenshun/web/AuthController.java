package com.tenshun.web;


import com.tenshun.repository.UserRepository;
import com.tenshun.service.user.UserService;
import com.tenshun.web.form.RegForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class AuthController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "error",required = false) String error,
                        @RequestParam(value = "logout",	required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid Credentials provided.");
        }

        if (logout != null) {
            model.addAttribute("message", "Logged out from {ProjectName} successfully.");
        }


        return "login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/join", method = RequestMethod.GET)
    public String signUpGet(Model model) {

        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != "anonymousUser") {

            return "redirect:/welcome";
        }
        RegForm form = new RegForm();
        model.addAttribute("regForm", form);
        return "join";
    }

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public String signUpPost(@ModelAttribute("regForm") @Valid RegForm regForm,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            return "join";
        }

        if (userRepository.findOneByLogin(regForm.getLogin().toLowerCase()).isPresent()) {
            model.addAttribute("loginExistsError", "Login already exists");
            return "join";
        } else if (userRepository.findOneByEmail(regForm.getEmail()).isPresent()) {
            model.addAttribute("emailExistsError", "Email already in use");
            return "join";
        } else {
            userService.createUser(regForm.getLogin(), regForm.getPassword(), regForm.getEmail());

            return "redirect:/welcome";
        }

    }


}
