select * from FE.Coverages, FE.People, FE.Offers, FE.Products, FE.CoveredPeople 
WHERE FE.Coverages.coverageId = FE.CoveredPeople.coverageId AND FE.CoveredPeople.personId = FE.People.personId AND FE.Coverages.offerId = FE.Offers.offerId
AND FE.Offers.productId = FE.Products.productId ORDER BY FE.People.personId;