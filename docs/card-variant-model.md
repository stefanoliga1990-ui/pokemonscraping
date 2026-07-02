# Card variant model

## CardTrader representation

CardTrader separates the product catalogue into expansions and blueprints. A marketplace product is an offer for a blueprint and adds editable properties such as language, condition, first edition, reverse, signed and altered. The graded flag is exposed directly on the marketplace product.

Observed Pokemon examples:

- `Base Set` and `Base Set Shadowless` are separate expansions;
- first edition and unlimited cards share a blueprint and differ through `first_edition`;
- reverse, signed and altered are values in `properties_hash`;
- graded is a top-level marketplace product value;
- condition belongs to a monitor and is not part of the physical card variant;
- language is selected before variant resolution and is part of the grouping key.

## Normalized domain model

`CardVariant` preserves the blueprint, expansion and all physical variant flags. `CardVariantGroup` contains alternatives that the user should compare before activating a monitor.

Variants are grouped by:

1. normalized expansion family;
2. normalized card name;
3. normalized collector number;
4. language.

An expansion ending in `Shadowless`, `- Shadowless` or `(Shadowless)` belongs to the same family as the expansion without that suffix. No expansion ID is hard-coded. `Base Set 2` therefore remains a separate family from `Base Set`.

Within a group, separate options are retained for:

- blueprint and blueprint version;
- first edition;
- shadowless;
- reverse;
- graded;
- signed;
- altered.

When a group contains historical edition alternatives, the standard non-first-edition and non-shadowless option is presented as `Unlimited`. In other groups it is presented as `Standard` only when a distinguishing label is required.

## Boundaries

Automatic grouping is deliberately conservative. Relationships that cannot be inferred from the expansion suffix, card name and collector number will require an explicit catalogue relation in a later step. Persistence and CardTrader JSON mapping are outside this model and will be implemented separately.
