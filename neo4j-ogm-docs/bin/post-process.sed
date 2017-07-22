# Remove erroneous 'emphasis' within 'literal'.
# These happen when Asciidoctor chokes on things like:
# `MATCH (a)-[b]->(c) RETURN *`
# if the same paragraph contains another query containing an asterisk.
# The two asterisks are interpreted as 'strong emphasis', which is wrong.
# It is also illegal DocBook, so it shouldn't happen.
s/\(<literal>[^<]*\)<emphasis[^>]*>/\1*/g
s/\(<literal>[^<]*\)<\/emphasis[^>]*>/\1*/g

# Remove 'preface' because it's wicked hard to exclude it from nav and toc.
s/<preface>//g
s/<\/preface>//g
s/<title><\/title>//
