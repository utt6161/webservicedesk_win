package com.wsd.ska.site;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site,Long> {
    Site findFirstByOrderByIdAsc();
}
