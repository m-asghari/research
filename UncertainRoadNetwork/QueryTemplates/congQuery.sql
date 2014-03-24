SELECT T1.TIME, T1.CONG STATUS1, T2.CONG STATUS2
FROM PATH##PATH_NUM##_EDGE_PATTERNS T1 INNER JOIN PATH##PATH_NUM##_EDGE_PATTERNS T2 ON T1.TIME = T2.TIME
WHERE T1."FROM" = ##FROM1## AND T2."FROM" = ##FROM2##
ORDER BY T1.TIME