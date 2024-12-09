package com.example.payment;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card")
public class CardController {

    @PostMapping("/validate")
    public String validateCard(@RequestBody CardRequest cardRequest) {
        // 간단히 카드 정보를 출력하고, 임의 메시지를 반환
        System.out.println("Card Number: " + cardRequest.getCardNumber());
        System.out.println("CVC: " + cardRequest.getCvc());
        System.out.println("Expiry Date: " + cardRequest.getExpiryDate());

        return "카드 정보가 처리되었습니다!";
    }
}
