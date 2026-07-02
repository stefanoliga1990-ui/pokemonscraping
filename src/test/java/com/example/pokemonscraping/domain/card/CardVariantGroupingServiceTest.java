package com.example.pokemonscraping.domain.card;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardVariantGroupingServiceTest {

    private final CardVariantGroupingService service = new CardVariantGroupingService();

    @Test
    void groupsBaseSetEditionsAndShadowlessAsAlternatives() {
        CardVariant unlimited = variant(111151, 1472, "Base Set", "004", "it", false, false);
        CardVariant firstEdition = variant(111151, 1472, "Base Set", "004", "it", true, false);
        CardVariant shadowless = variant(211151, 1969, "Base Set Shadowless", "4", "it", false, false);

        List<CardVariantGroup> groups = service.group(List.of(unlimited, firstEdition, shadowless));

        assertThat(groups).hasSize(1);
        CardVariantGroup group = groups.get(0);
        assertThat(group.expansionFamilyName()).isEqualTo("Base Set");
        assertThat(group.isAmbiguous()).isTrue();
        assertThat(group.variants()).extracting(CardVariant::edition)
                .containsExactly(CardEdition.STANDARD, CardEdition.FIRST_EDITION, CardEdition.SHADOWLESS);
        assertThat(group.options()).extracting(CardVariantOption::label)
                .containsExactly("Unlimited", "First Edition", "Shadowless");
    }

    @Test
    void keepsBaseSetTwoInASeparateGroup() {
        CardVariant baseSet = variant(111151, 1472, "Base Set", "004", "it", false, false);
        CardVariant baseSetTwo = variant(311151, 1478, "Base Set 2", "004", "it", false, false);

        List<CardVariantGroup> groups = service.group(List.of(baseSet, baseSetTwo));

        assertThat(groups).hasSize(2);
        assertThat(groups).extracting(CardVariantGroup::expansionFamilyName)
                .containsExactlyInAnyOrder("Base Set", "Base Set 2");
    }

    @Test
    void keepsLanguagesInSeparateGroups() {
        CardVariant italian = variant(111151, 1472, "Base Set", "004", "it", false, false);
        CardVariant english = variant(111151, 1472, "Base Set", "004", "en", false, false);

        List<CardVariantGroup> groups = service.group(List.of(italian, english));

        assertThat(groups).hasSize(2);
        assertThat(groups).extracting(CardVariantGroup::language).containsExactly("en", "it");
    }

    @Test
    void exposesTechnicalPropertiesAsDistinctOptions() {
        CardVariant standard = variant(111151, 1472, "Base Set", "004", "it", false, false);
        CardVariant reverseGraded = new CardVariant(
                111151, 1472, "Base Set", "Charizard", "004", null, "it",
                false, false, true, true, false, false
        );

        CardVariantGroup group = service.group(List.of(standard, reverseGraded)).get(0);

        assertThat(group.options()).extracting(CardVariantOption::label)
                .containsExactly("Standard", "Reverse · Graded");
    }

    @Test
    void removesDuplicateVariantsCreatedByMultipleOffers() {
        CardVariant candidate = variant(111151, 1472, "Base Set", "004", "it", false, false);

        CardVariantGroup group = service.group(List.of(candidate, candidate)).get(0);

        assertThat(group.variants()).containsExactly(candidate);
        assertThat(group.isAmbiguous()).isFalse();
    }

    @Test
    void addsABlueprintFallbackWhenTwoAlternativesHaveTheSameLabel() {
        CardVariant firstBlueprint = variant(111151, 1472, "Base Set", "004", "it", false, false);
        CardVariant secondBlueprint = variant(211151, 1472, "Base Set", "004", "it", false, false);

        CardVariantGroup group = service.group(List.of(firstBlueprint, secondBlueprint)).get(0);

        assertThat(group.options()).extracting(CardVariantOption::label)
                .containsExactly("Standard · Variant 111151", "Standard · Variant 211151");
    }

    @Test
    void recognizesSupportedShadowlessExpansionNamesWithoutHardCodedIds() {
        assertThat(ExpansionVariantClassifier.isShadowless("Base Set Shadowless")).isTrue();
        assertThat(ExpansionVariantClassifier.isShadowless("Base Set - Shadowless")).isTrue();
        assertThat(ExpansionVariantClassifier.isShadowless("Base Set (Shadowless)")).isTrue();
        assertThat(ExpansionVariantClassifier.familyName("Base Set (Shadowless)")).isEqualTo("Base Set");
        assertThat(ExpansionVariantClassifier.isShadowless("Base Set 2")).isFalse();
    }

    private CardVariant variant(
            long blueprintId,
            long expansionId,
            String expansionName,
            String collectorNumber,
            String language,
            boolean firstEdition,
            boolean shadowless
    ) {
        return new CardVariant(
                blueprintId,
                expansionId,
                expansionName,
                "Charizard",
                collectorNumber,
                null,
                language,
                firstEdition,
                shadowless,
                false,
                false,
                false,
                false
        );
    }
}
