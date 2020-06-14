package com.wsd.ska.global_model_attrib;

import com.wsd.ska.site.Site;
import com.wsd.ska.site.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAttribAdvice {

    @Autowired
    private SiteRepository siteRepository;

    @ModelAttribute("site")
    public Site populateSite() {
        return siteRepository.findFirstByOrderByIdAsc();
    }
}
