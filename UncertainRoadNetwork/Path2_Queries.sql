CREATE TABLE PATH2_EDGES (
  "FROM" NUMBER,
  "TO" NUMBER,
  DISTANCE NUMBER,
  MAX_SPEED NUMBER
);
INSERT INTO PATH2_EDGES VALUES (768701, 774344, 1.36762262960347, 65.0);
INSERT INTO PATH2_EDGES VALUES (774344, 770599, .444978561879933, 65.0);
INSERT INTO PATH2_EDGES VALUES (770599, 768297, .509714021227737, 65.0);
INSERT INTO PATH2_EDGES VALUES (768297, 768283, 1.22949229952047, 65.0);
INSERT INTO PATH2_EDGES VALUES (768283, 770587, .532433285394738, 65.0);
INSERT INTO PATH2_EDGES VALUES (770587, 770012, .617515715778045, 65.0);
INSERT INTO PATH2_EDGES VALUES (770012, 770024, .831888557995132, 65.0);
INSERT INTO PATH2_EDGES VALUES (770024, 770036, .822033436288788, 65.0);
INSERT INTO PATH2_EDGES VALUES (770036, 770354, .73694808993998, 65.0);
INSERT INTO PATH2_EDGES VALUES (770354, 770048, .369202948483409, 65.0);
INSERT INTO PATH2_EDGES VALUES (770048, 770331, .145218164366512, 65.0);
INSERT INTO PATH2_EDGES VALUES (770331, 770544, .89781780061275, 65.0);
INSERT INTO PATH2_EDGES VALUES (770544, 770061, .71286599330717, 65.0);
INSERT INTO PATH2_EDGES VALUES (770061, 770556, .763449325523628, 65.0);
INSERT INTO PATH2_EDGES VALUES (770556, 770076, .86788694657905, 65.0);
INSERT INTO PATH2_EDGES VALUES (770076, 771202, 1.1111623626603, 65.0);
INSERT INTO PATH2_EDGES VALUES (771202, 770089, .281609509242786, 65.0);
INSERT INTO PATH2_EDGES VALUES (770089, 770103, .248187971493571, 65.0);
INSERT INTO PATH2_EDGES VALUES (770103, 771636, .539524976254178, 65.0);
INSERT INTO PATH2_EDGES VALUES (771636, 770475, 1.04605556678403, 65.0);
INSERT INTO PATH2_EDGES VALUES (770475, 770487, .544006096165415, 65.0);
INSERT INTO PATH2_EDGES VALUES (770487, 770116, .75902803721167, 65.0);
INSERT INTO PATH2_EDGES VALUES (770116, 769895, 1.4494295210997, 65.0);
INSERT INTO PATH2_EDGES VALUES (769895, 769880, 1.13622897902183, 65.0);
INSERT INTO PATH2_EDGES VALUES (769880, 769866, .524990959764892, 65.0);
INSERT INTO PATH2_EDGES VALUES (769866, 769847, 1.08232612590485, 65.0);
INSERT INTO PATH2_EDGES VALUES (769847, 768230, .440176960588211, 65.0);
INSERT INTO PATH2_EDGES VALUES (768230, 767610, 1.32202021465201, 65.0);
INSERT INTO PATH2_EDGES VALUES (767610, 767598, .935710639700587, 65.0);
INSERT INTO PATH2_EDGES VALUES (767598, 718076, .653763451202741, 65.0);
INSERT INTO PATH2_EDGES VALUES (718076, 767471, .448224057848118, 65.0);
INSERT INTO PATH2_EDGES VALUES (767471, 718072, .541101105541897, 65.0);
INSERT INTO PATH2_EDGES VALUES (718072, 767454, .395947585407034, 65.0);
INSERT INTO PATH2_EDGES VALUES (767454, 762329, .317393048326709, 65.0);
INSERT INTO PATH2_EDGES VALUES (762329, 767621, .294697779233858, 65.0);
INSERT INTO PATH2_EDGES VALUES (767621, 767573, .36179647968209, 65.0);
INSERT INTO PATH2_EDGES VALUES (767573, 718066, .456542987879065, 65.0);
INSERT INTO PATH2_EDGES VALUES (718066, 767542, .701318831701775, 65.0);
INSERT INTO PATH2_EDGES VALUES (767542, 718064, .451565211885963, 65.0);
INSERT INTO PATH2_EDGES VALUES (718064, 767495, .695783026161928, 65.0);
INSERT INTO PATH2_EDGES VALUES (767495, 718375, .213526556842471, 65.0);
INSERT INTO PATH2_EDGES VALUES (718375, 716955, .430313853965351, 65.0);
INSERT INTO PATH2_EDGES VALUES (716955, 718370, .508826043255952, 65.0);
INSERT INTO PATH2_EDGES VALUES (718370, 716949, .658741593045558, 65.0);
INSERT INTO PATH2_EDGES VALUES (716949, 760650, .641751012718177, 65.0);
INSERT INTO PATH2_EDGES VALUES (760650, 718045, .683680626496052, 65.0);
INSERT INTO PATH2_EDGES VALUES (718045, 718173, 1.17081999658844, 65.0);
INSERT INTO PATH2_EDGES VALUES (718173, 760643, .798857244188759, 65.0);
INSERT INTO PATH2_EDGES VALUES (760643, 760635, .339568556963253, 65.0);
INSERT INTO PATH2_EDGES VALUES (760635, 774671, .301802282356639, 65.0);
INSERT INTO PATH2_EDGES VALUES (774671, 718166, .351829274876773, 65.0);
INSERT INTO PATH2_EDGES VALUES (718166, 764037, .633851085472667, 65.0);
CREATE TABLE PATH2_SPEED_PATTERNS AS
SELECT *
FROM SPEED_PATTERNS_2013 T1
WHERE T1.LINK_ID IN (SELECT LINK_ID FROM PATH2_SENSORS);
CREATE INDEX PATH2_SPEED_PATTERNS_TIME_IDX ON PATH2_SPEED_PATTERNS(TIME);
CREATE INDEX PATH2_SPEED_PATTERNS_LINK_IDX ON PATH2_SPEED_PATTERNS(LINK_ID);
COMMIT;
CREATE TABLE TEMP AS
SELECT T1."FROM", T1."TO", T2.TIME FROM_TIME, T3.TIME TO_TIME, T2.SPEED FROM_SPEED, T3.SPEED TO_SPEED, T1.DISTANCE, T1.MAX_SPEED
FROM PATH2_EDGES T1, (PATH2_SPEED_PATTERNS T2 FULL OUTER JOIN PATH2_SPEED_PATTERNS T3 ON T2.TIME = T3.TIME)
WHERE T1."FROM" = T2.LINK_ID AND T1."TO" = T3.LINK_ID;
UPDATE TEMP SET FROM_TIME = TO_TIME, FROM_SPEED = TO_SPEED WHERE FROM_TIME = NULL;
UPDATE TEMP SET TO_TIME = FROM_TIME, TO_SPEED = FROM_SPEED WHERE TO_TIME = NULL;
COMMIT;
CREATE TABLE PATH2_EDGE_PATTERNS AS
SELECT "FROM", "TO", FROM_TIME AS TIME, TO_CHAR(FROM_TIME, 'DDD') AS DAY, TO_CHAR(FROM_TIME,'hh24:mi:ss') AS TOD, (DISTANCE*2*60)/(FROM_SPEED + TO_SPEED) AS TRAVEL_TIME,
CASE WHEN (DISTANCE*2)/(FROM_SPEED + TO_SPEED) > (DISTANCE*100)/(60*MAX_SPEED) THEN 'TRUE' ELSE 'FALSE' END AS CONG 
FROM TEMP;
COMMIT;
CREATE INDEX PATH2_EDGE_PATTERNS_TIME_IDX ON PATH2_EDGE_PATTERNS(TIME);
CREATE INDEX PATH2_EDGE_PATTERNS_TOD_IDX ON PATH2_EDGE_PATTERNS(TOD);
DROP TABLE TEMP;
COMMIT;