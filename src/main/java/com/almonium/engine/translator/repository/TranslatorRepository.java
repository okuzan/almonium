package com.almonium.engine.translator.repository;

import com.almonium.engine.translator.model.entity.Translator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatorRepository extends JpaRepository<Translator, Long> {
    Optional<Translator> getByName(String name);

    default Translator getYandex() {
        return this.getByName("YANDEX").orElseThrow();
    }

    default Translator getGoogle() {
        return this.getByName("GOOGLE").orElseThrow();
    }
}
