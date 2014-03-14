SELECT AVG(SP.SPEED)
FROM (  SELECT SPEED
        FROM PATH_SPEED_PATTERNS
        WHERE LINK_ID = '##FROM##' 
              AND
              TIME IN ( SELECT MAX(TIME)
                        FROM PATH_SPEED_PATTERNS
                        WHERE LINK_ID = '##FROM##' AND TIME < '##TIME##')
        UNION
        SELECT SPEED
        FROM PATH_SPEED_PATTERNS
        WHERE LINK_ID = '##TO##' 
              AND
              TIME IN ( SELECT MAX(TIME)
                        FROM PATH_SPEED_PATTERNS
                        WHERE LINK_ID = '##TO##' AND TIME < '##TIME##')) SP
