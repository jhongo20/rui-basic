package com.rui.basic.app.basic.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.rui.basic.app.basic.domain.entities.RuiRoles;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.service.RoleService;
import com.rui.basic.app.basic.service.UserService;
import com.rui.basic.app.basic.web.dto.RoleDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DashboardController {

    // Inyecciones de dependencias útiles
    @Autowired
    private UserService userService;  // Para manejar usuarios

    @Autowired
    private RoleService roleService;  // Para manejar roles
    

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                // Información básica del usuario
                RuiUser currentUser = userService.findByUsername(username);
                model.addAttribute("currentUser", currentUser);
                
                // Estadísticas
                model.addAttribute("totalUsers", userService.countUsers());
                model.addAttribute("activeUsers", userService.countActiveUsers());
                
                // Roles y conteos
                List<RuiRoles> roles = roleService.getAllRoles();
                Map<String, Long> usersByRole = userService.getUserCountByRole();
                
                model.addAttribute("roles", roles);
                model.addAttribute("usersByRole", usersByRole);
                
                log.debug("Dashboard cargado para usuario: {}", username);
                log.debug("Usuarios por rol: {}", usersByRole);
            }
            return "dashboard";
        } catch (Exception e) {
            log.error("Error al cargar el dashboard: ", e);
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "error";
        }
    }

    // Agrega más métodos para manejar las diferentes secciones
    @GetMapping("/users")
    public String users(Model model) {
        // Obtener lista de usuarios
        List<RuiUser> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        // Estadísticas de usuarios
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", users.stream()
            .filter(user -> user.getStatus() == 1)
            .count());
        
        return "users";
    }

    @GetMapping("/user/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        RuiUser user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user-detail";
    }

    @GetMapping("/roles")
    public String roles(Model model) {
        List<RuiRoles> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        return "roles";
    }

    @PostMapping("/role/create")
    public String createRole(@ModelAttribute RoleDto roleDto) {
        roleService.createRole(roleDto);
        return "redirect:/roles";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }
}
