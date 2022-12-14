package com.delgo.reward.repository;


import com.delgo.reward.domain.Mungple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;
import java.util.Optional;

public interface MungpleRepository extends JpaRepository<Mungple, Integer>, JpaSpecificationExecutor<Mungple> {
    Optional<Mungple> findByMungpleId(int mungpleId);

    Optional<Mungple> findByLatitudeAndLongitude(String latitude, String longitude);

    List<Mungple> findByCategoryCode(String categoryCode);

}
