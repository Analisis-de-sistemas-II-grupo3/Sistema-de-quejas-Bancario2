   package com.banco.quejas;
   import org.springframework.stereotype.Controller;
   import org.springframework.web.bind.annotation.GetMapping;
/** CU00 - Portal público y punto de entrada al sistema. */
@Controller
public class PortalController {
    @GetMapping("/")
    public String mostrarInicio() {
        return "inicio";
    }
}