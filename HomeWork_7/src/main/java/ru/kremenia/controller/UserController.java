package ru.kremenia.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kremenia.persist.User;
import ru.kremenia.persist.UserRepository;


import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;



@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    public String listPage(
            @RequestParam(required = false) String usernameFilter,
            @RequestParam(required = false) String emailFilter,
            Model model) {
        usernameFilter = usernameFilter == null || usernameFilter.isEmpty() ? null : "%" + usernameFilter + "%";
        emailFilter = emailFilter == null || emailFilter.isEmpty() ? null : "%" + emailFilter + "%";
        model.addAttribute("users", userRepository.usersByFilter(usernameFilter, emailFilter));
        return "user";
    }

    @GetMapping("/{id}")
    public String form(@PathVariable("id") long id, Model model) {
        model.addAttribute("user", userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found")));
        return "user_form";
    }

    @GetMapping("/new")
    public String addNewUser(Model model){
        model.addAttribute("user", new User(""));
        return "user_form";
    }

    @GetMapping("/delete/{id}")
    public String deleteUserById(@PathVariable long id){
        userRepository.deleteById(id);
        return "redirect:/user";
    }

    @PostMapping
    public String saveUser(@Valid User user, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "user_form";
        }
        if (!user.getPassword().equals(user.getMatchingPassword())){
            bindingResult.rejectValue("password", "Password not match");
            return "user_form";
        }
        userRepository.save(user);
        return "redirect:/user";
    }

    @PostMapping("/update")
    public String updateUser(User user){
        userRepository.save(user);
        return "redirect:/user";
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFoundExceptionHandler(Model model, EntityNotFoundException e) {
        model.addAttribute("message", e.getMessage());
        return "not_found";
    }
}
