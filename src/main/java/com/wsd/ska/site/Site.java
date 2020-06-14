package com.wsd.ska.site;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String company_name;

    private String phone_number;

    private String email;

    public Site(String phone_number, String company_name, String email){
        this.company_name = company_name;
        this.phone_number = phone_number;
        this.email = email;
    }
}
