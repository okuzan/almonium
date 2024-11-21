package com.almonium.user.core.service;

import com.almonium.analyzer.translator.model.enums.Language;
import com.almonium.user.core.model.entity.Learner;
import java.util.Set;

public interface LearnerService {
    void setupLanguages(Set<Language> fluentLangs, Set<Language> targetLangs, Learner user);

    void removeTargetLanguage(Language code, long learnerId);

    void addTargetLanguage(Language code, long learnerId);

    void updateFluentLanguages(Set<Language> dto, Learner user);
}
