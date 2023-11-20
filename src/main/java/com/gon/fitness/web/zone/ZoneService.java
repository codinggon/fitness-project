package com.gon.fitness.web.zone;

import com.gon.fitness.domain.Zone;
import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.settings.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneService {

    private final ZoneRepository zoneRepository;

    //@ApplicationRunner : 어플리케이션이 실행되고 나서 동작시키는 방식
    //@PostConstruct 빈이 생성되면서 실행되는 방식
    //위에 2가지 방식중에 원하는 걸로 하면 된다. -> bean이 초기화 되는 시점에 실행됨
    @PostConstruct
    public void initZoneData() throws IOException {
        if (zoneRepository.count() == 0) {
            Resource resource = new ClassPathResource("zones_kr.csv");
            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream().map(line -> {
                String[] split = line.split(",");
                return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
            }).collect(Collectors.toList());
            zoneRepository.saveAll(zoneList);
        }
    }

    public void addZones(ZoneForm zoneForm, Account account) {
        Zone targetZone = getTargetZone(zoneForm);
        account.getZones().add(targetZone);
    }

    public void removeZones(ZoneForm zoneForm, Account account) {
        Zone targetZone = getTargetZone(zoneForm);
        account.getZones().remove(targetZone);
    }

    private Zone getTargetZone(ZoneForm zoneForm) {
        String restoreCity = zoneForm.getZoneValue().substring(0, zoneForm.getZoneValue().indexOf("("));
        String restoreLocalNameOfCity = zoneForm.getZoneValue().substring(zoneForm.getZoneValue().indexOf("(") + 1, zoneForm.getZoneValue().indexOf(")"));
        String restoreProvince = zoneForm.getZoneValue().substring(zoneForm.getZoneValue().indexOf("/") + 1);

        Zone targetZone = zoneRepository.findByCityAndLocalNameOfCityAndProvince(restoreCity,restoreLocalNameOfCity,restoreProvince);
        return targetZone;
    }

}

















