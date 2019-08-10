package com.ecommerce.microcommerce.service;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    public int calculMarge(int prixVente, int prixAchat) {
        return prixVente - prixAchat;
    }
}
