select FE.Coverages.coverageId, FE.Coverages.benefit, FE.People.firstName, FE.People.lastName,FE.People.ssn, FE.People.gender, FE.People.dateOfBirth, FE.Products.solidifyId, 
FE.ElectionTypes.name AS enrolled, FE.Coverages.annualPremium, FE.Coverages.modalPremium, IFNULL(FE.DependentsToEmployees.relationship, 'EMPLOYEE') AS relationship, FE.Coverages.pending, IFNULL(FE.Coverages.declineReason, 'N/A') AS declineReason
FROM FE.Coverages, FE.People LEFT JOIN FE.DependentsToEmployees ON FE.People.personId = FE.DependentsToEmployees.dependentId, FE.Offers, FE.Products, FE.CoveredPeople, FE.ElectionTypes
WHERE FE.ElectionTypes.electionTypeId = FE.Coverages.electionTypeId AND FE.Coverages.coverageId = FE.CoveredPeople.coverageId AND FE.CoveredPeople.personId = FE.People.personId AND FE.Coverages.offerId = FE.Offers.offerId
AND FE.Offers.productId = FE.Products.productId  ORDER BY FE.People.personId;