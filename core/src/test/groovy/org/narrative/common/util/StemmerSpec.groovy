package org.narrative.common.util

import spock.lang.Specification

class StemmerSpec extends Specification {


    def "Get stemmed text with no punctuation"(originalText, stemmedText){
        expect:
            Stemmer.getStemmedText(originalText) == stemmedText

        where:
            originalText            ||          stemmedText
            "A Test String"         ||          "a test string"
            "SomethingElse"         ||          "somethingels"
            "C∆røaπz∞y˚˚∆˙∫¥¨¨ƒ˙©"  ||          "c røaπz y ƒ"
            ""                      ||          ""

            "Los futurólogos vieron los comprensivos comentarios de este par como una " +
                "señal de que la revisión de antecedentes suplementaria del FBI " +
                "sobre Kavanaugh"   ||
                    "lo futurólogo vieron lo comprensivo comentario" +
                " de est par como una señal de que la revisión de antecedent suplementaria " +
                "del fbi sobr kavanaugh"

            "Mit der CNN.com Europe Edition verfügt der TV-Sender CNN über eine umfassende Webseite, " +
                "die im Minutentakt rund um die Uhr aktualisierte Weltnachrichten aus einer " +
                "europäischen Perspektive auf den Bildschirm bringt. Wichtige, globale Ereignisse, " +
                "geordnet in den Rubriken breaking news, current news headlines oder in depth, " +
                "werden mit Hilfe von Video- und Audioclips, Bildern, Karten, Profilen, Zeitachsen " +
                "und Tatsachenberichten fundiert dargestellt." ||
                    "mit der cnn com europ edit verfügt " +
                "der tv sender cnn über ein umfassend webseit die im minutentakt rund um die uhr " +
                "aktualisiert weltnachrichten au einer europäischen perspekt auf den bildschirm " +
                "bringt wichtig global ereigniss geordnet in den rubriken break new current new " +
                "headlin oder in depth werden mit hilf von video und audioclip bildern karten " +
                "profilen zeitachsen und tatsachenberichten fundiert dargestellt"

            "Discussing conspiracy theories including government coverups, suppressed technology, ancient aliens, UFOS and extraterrestrials" ||
                    "discuss conspiraci theori includ govern coverup suppress technolog ancient alien ufo and extraterrestri"
            "All things where people fight for truth and justice. Conspiracy, health, geopolitics, exo-politics, financial, human rights etc." ||
                    "all thing where peopl fight for truth and justic conspiraci health geopolit exo polit financi human right etc"
            "Fictional books based on the religious conspiracies, symbology, and mythology of secret societies and world religions." ||
                    "fiction book base on the religi conspiraci symbolog and mytholog of secret societi and world religion"
            "Conspiracy Theories" || "conspiraci theori"

    }


}
