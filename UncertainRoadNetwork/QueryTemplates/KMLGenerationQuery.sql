SELECT DISTINCT SC.LINK_ID, t.Y, t.X, SC.DIRECTION 
FROM SENSOR_CONFIG SC, table(SDO_UTIL.GETVERTICES(SC.START_LAT_LONG)) t
WHERE SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##5MIN##')
      AND
      SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##10MIN##')
      AND
      SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##15MIN##')
      AND
      SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##30MIN##')
      AND
      SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##60MIN##')
      AND
      SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##90MIN##')
      AND
      SC.LINK_ID IN ( SELECT LINK_ID
                      FROM SENSOR_TOD_STAT 
                      WHERE STD_DEV_SPEED > 10 AND "COUNT" > 200 AND TOD = '##120MIN##')                      
ORDER BY SC.LINK_ID