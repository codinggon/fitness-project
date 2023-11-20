package com.gon.fitness.domain.settings;

import com.gon.fitness.domain.Tag;
import com.gon.fitness.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ZoneRepository extends JpaRepository<Zone, Long> {


    Zone findByCityAndLocalNameOfCityAndProvince(String restoreCity, String restoreLocalNameOfCity, String restoreProvince);
}
