package com.linguatool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.linguatool.client.*;
import com.linguatool.model.dto.external_api.request.AnalysisDto;
import com.linguatool.model.dto.external_api.response.datamuse.DatamuseEntryDto;
import com.linguatool.model.dto.external_api.response.google.GoogleDto;
import com.linguatool.model.dto.external_api.response.wordnik.WordnikAudioDto;
import com.linguatool.model.dto.external_api.response.words.WordsReportDto;
import com.linguatool.model.dto.external_api.response.yandex.YandexDto;
import com.linguatool.model.dto.lang.POS;
import com.linguatool.model.dto.lang.translation.MLTranslationCard;
import com.linguatool.model.dto.lang.translation.TranslationCardDto;
import com.linguatool.model.entity.lang.Language;
import com.linguatool.model.entity.user.User;
import com.linguatool.model.mapping.DictionaryDtoMapper;
import com.linguatool.repository.LangPairTranslatorRepository;
import com.linguatool.repository.LanguageRepository;
import com.linguatool.repository.TranslatorRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class LanguageProcessor {

    UrbanClient urbanClient;
    DatamuseClient datamuseClient;
    GoogleClient googleClient;
    WordnikClient wordnikClient;
    YandexClient yandexClient;
    WordsClient wordsClient;
    OxfordClient oxfordClient;
    ObjectMapper objectMapper;
    FDClient fdClient;
    CoreNLPService coreNLPService;
    GoogleTranslationService googleService;
    LangPairTranslatorRepository langPairTranslatorRepository;
    LanguageRepository languageRepository;
    TranslatorRepository translatorRepository;
    DictionaryDtoMapper dictionaryDtoMapper;

    private static final double SCALE = 1.153315895823627;
    private static final double OFFSET = 10;
    private static final double LOW_BOUND = 1e-9;

    public ResponseEntity<?> limitExceeded(String provider) {
        return ResponseEntity.status(403).body(provider);
    }
//    public TranslationCardDto translate(String entry, String sourceLang, String targetLang) {
//        return this.translate(entry, Language.fromString(sourceLang), Language.fromString(targetLang));
//    }

    public MLTranslationCard bulkTranslate(String text, Language targetLang) {

        //todo deepL
        return MLTranslationCard.builder()
                .provider(translatorRepository.getGoogle().getName())
                .text(googleService.bulkTranslateText(text, targetLang.getCode()))
                .build();
    }

    @SneakyThrows
    public TranslationCardDto translate(String entry, Language sourceLang, Language targetLang) {

        if (sourceLang == null || targetLang == null) {
            return null;
        }

        List<Long> translatorsIds = langPairTranslatorRepository.getByLangFromAndLangTo(
                languageRepository.findByCode(sourceLang).orElseThrow().getId(),
                languageRepository.findByCode(targetLang).orElseThrow().getId());

        if (translatorsIds.size() == 0) {
            return null;
        } else if (translatorsIds.size() == 1) {
            if (translatorsIds.get(0).equals(translatorRepository.getYandex().getId())) {
                ResponseEntity<YandexDto> responseEntity = yandexClient.translate(entry, sourceLang, targetLang);

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    TranslationCardDto card = dictionaryDtoMapper.yandexToGeneral(responseEntity.getBody());
                    card.setProvider(translatorRepository.getYandex().getName());
                    return card;
                }
                if (responseEntity.getStatusCode() == HttpStatus.FORBIDDEN) {
//                    return limitExceeded(translatorRepository.getYandex().getName());
                    log.error("LIMIT EXCEEDED");
                    return null;
                } else if (responseEntity.getStatusCode() == HttpStatus.NOT_IMPLEMENTED) {
                    log.error("Language pair not supported: probably, langPairTranslator table is out of date");
                    throw new Exception("Unexpectedly not supported lang pair in this provider");
                } else if (responseEntity.getStatusCode().is4xxClientError()) {
                    log.error(responseEntity.getBody().toString());
//                    return ResponseEntity.internalServerError().body(responseEntity.getBody());
                    return null;
                } else {
                    return null;
                }

            } else {
                log.error("Cannot recognize translator");
//                return ResponseEntity.internalServerError().build();
                return null;
            }
        } else {
            // TODO if multiple => which engines?
            return null;
        }
    }

    public void create(String word) {
//        ResponseEntity<UrbanResponse> response = urbanClient.submit(word);
//        ResponseEntity<WordnikFrequencyDto> response = wordnikClient.submit(word);
//        ResponseEntity response = oxfordClient.submit(word);
//        ResponseEntity<List<FDEntry>> response = fdClient.request(word);
//        System.out.println("fd" + response);
//        ResponseEntity<List<DatamuseEntryDto>> response = datamuseClient.request(word);
//        UrbanResponse dto = response.getBody();
//        System.out.println((dto.getList().size()));

    }

    public String[] homophones() {
//        ResponseEntity<List<DatamuseEntryDto>> response = datamuseClient.request(entry);
        return null;
    }

    public List<String> getAudioLink(String word) {
        return Objects.requireNonNull(wordnikClient.getAudioFile(word).getBody())
                .stream().map(WordnikAudioDto::getFileUrl).collect(Collectors.toList());
    }

    @Deprecated
    public double getFrequencyDatamuse(String entry) {
        ResponseEntity<List<DatamuseEntryDto>> response = datamuseClient.getWordReport(entry);
        String fTag = Arrays.stream(response.getBody().get(0).getTags()).filter(tag -> tag.startsWith("f")).findFirst().get();
        return Double.parseDouble(fTag.split(":")[1]);
    }

    public Double getFrequency(String entry, Language language) {
        List<GoogleDto> list = googleClient.get(entry, language).getBody();
        return (list.size() > 1) ? Double.parseDouble(list.get(0).getTimeseries()[0]) : null;
    }

    public String[] getNounsForAdjective(String entry) {
        return Objects.requireNonNull(datamuseClient.getNounsForAdjective(entry)
                .getBody()).stream().map(DatamuseEntryDto::getWord).map(String::new).toArray(String[]::new);
    }

    public String[] getHomophones(String entry) {
        return Objects.requireNonNull(datamuseClient.getHomophones(entry)
                .getBody()).stream().map(DatamuseEntryDto::getWord).map(String::new).toArray(String[]::new);
    }

    public String[] getAdjectivesForNoun(String entry) {
        return Objects.requireNonNull(datamuseClient.getNounsForAdjective(entry)
                .getBody()).stream().map(DatamuseEntryDto::getWord).map(String::new).toArray(String[]::new);
    }

    private void singleWordAnalysis(AnalysisDto analysisDto,
                                    String entry,
                                    List<POS> posTags,
                                    List<String> lemmas,
                                    Language from,
                                    Language to
    ) {
        if (posTags.get(0).equals(POS.ADJECTIVE_COMPARATIVE)) {
            getBaseAdjectiveForComparative(entry);
        } else if (posTags.get(0).equals(POS.ADJECTIVE_SUPERLATIVE)) {
            getBaseAdjectiveForSuperlative(entry);
        } else if (posTags.get(0).equals(POS.PROPER_NOUN_SINGULAR) || posTags.get(0).equals(POS.PROPER_NOUN_PLURAL)) {
            analysisDto.setIsProper(true);
        } else if (posTags.get(0).equals(POS.FOREIGN_WORD)) {
            analysisDto.setIsForeignWord(true);
        } else if (posTags.get(0).equals(POS.NOUN_PLURAL)) {
            analysisDto.setIsPlural(true);
        } else if (posTags.get(0).isAdjective()) {
            analysisDto.setNouns(getNounsForAdjective(entry));
        } else if (posTags.get(0).equals(POS.NOUN)) {
            analysisDto.setAdjectives(getAdjectivesForNoun(entry));
        }
        analysisDto.setTranslationCards(this.translate(entry, from, to));
        analysisDto.setHomophones(getHomophones(entry));
//        dto.setFamily(getWordFamily(entry));
    }

    public String[] getWordFamily(String entry) {
        throw new NotImplementedException("not yet");
    }

    public String[] getSyllables(WordsReportDto dto) {
//        wordsClient.getReport()
        return dto.getSyllables().getList();
    }

    public AnalysisDto getReport(String entry, String languageCode, User user) {
        AnalysisDto analysisDto = new AnalysisDto();
        List<String> lemmas = coreNLPService.lemmatize(entry);
        analysisDto.setLemmas(lemmas.stream().map(String::new).toArray(String[]::new));

        Language sourceLang = Language.fromString(languageCode);
        Language fluentLanguage = user.getFluentLanguages().iterator().next().getCode();

        List<POS> posTags = coreNLPService.posTagging(entry);
        analysisDto.setPosTags(posTags.stream().map(POS::toString).toArray(String[]::new));
        Double freq = getFrequency(entry, sourceLang);
        if (freq != null) {
            analysisDto.setFrequency(calculateRelativeFrequency(freq));
        }

        if (lemmas.size() != posTags.size()) {
            log.error("Lemmas don't correspond with POS tags");
        }
        if (lemmas.size() == 1) {
            log.info("one lemma analysis");
            singleWordAnalysis(analysisDto, entry, posTags, lemmas, sourceLang, fluentLanguage);
        } else if (lemmas.size() == 2) {
            // TO VERB case
            if (posTags.get(0).equals(POS.TO) && posTags.get(1).equals(POS.VERB)) {
                entry = lemmas.get(1);
                singleWordAnalysis(analysisDto, entry, posTags, lemmas, sourceLang, fluentLanguage);
            }
        }
        return analysisDto;
    }

    public WordsReportDto getRandom() {
        return wordsClient.getRandomWord().getBody();
    }

    public String getBaseAdjectiveForComparative(String adj) {
        throw new NotImplementedException("not yet");
    }

    public String getBaseAdjectiveForSuperlative(String adj) {
        throw new NotImplementedException("not yet");
    }

    public ByteString textToSpeech(String code, String text) {
        return googleService.textToSpeech(code, text);
    }

    private static double calculateRelativeFrequency(double frequency) {
        if (frequency == 0) return 0;
        if (frequency < LOW_BOUND) return 1;
        return SCALE * (Math.log10(frequency) + OFFSET);
    }
}
