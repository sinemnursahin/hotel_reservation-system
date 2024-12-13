package com.hotelprject.hotelproject.controller;

import com.hotelprject.hotelproject.model.Room;
import com.hotelprject.hotelproject.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BaseController {


    @GetMapping
    public String basePage() {
        return "home";
    }



}