package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Controller
public class SismoController {

    private final SismoService sismoService;

    @Autowired
    public SismoController(SismoService sismoService) {
        this.sismoService = sismoService;
    }

    @GetMapping("/dasdasdsa")
    public String index(Model model) {
        Set<Integer> years = sismoService.getAvailableYears();
        model.addAttribute("years", years);
        return "index";
    }

    @GetMapping("/mapa")
    public String mapa(@RequestParam(required = false) Integer year, Model model) {
        Set<Integer> years = sismoService.getAvailableYears();
        model.addAttribute("years", years);
        model.addAttribute("selectedYear", year);
        return "mapa";
    }

    @GetMapping("/api/sismos")
    @ResponseBody
    public List<Sismo> getSismos(@RequestParam(required = false) Integer year) {
        if (year != null) {
            return sismoService.getSismosByYear(year);
        } else {
            return sismoService.getAllSismos();
        }
    }
}