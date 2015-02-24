select FE.Groups.name AS groupName, FE.Coverages.benefit, FE.People.firstName, FE.People.lastName,FE.People.ssn, FE.People.gender, FE.People.dateOfBirth, FE.Products.solidifyId, 
FE.ElectionTypes.name AS enrolled, 
IFNULL(FE.DependentsToEmployees.relationship, 'EMPLOYEE') AS relationship, 
CASE WHEN FE.DependentsToEmployees.relationship IS NULL OR FE.Products.solidifyId = 'assurantVtl' OR FE.Products.solidifyId = 'assurantCi' THEN FE.Coverages.annualPremium ELSE 0 END AS annualPremium, 
CASE WHEN FE.DependentsToEmployees.relationship IS NULL OR FE.Products.solidifyId = 'assurantVtl' OR FE.Products.solidifyId = 'assurantCi' THEN FE.Coverages.modalPremium ELSE 0 END AS modalPremium, 
FE.Coverages.pending, IFNULL(FE.Coverages.declineReason, 'N/A') AS declineReason, FE.Coverages.start AS effectiveDate, IFNULL(FE.Apps.enroller, 'N/A') AS enroller
FROM FE.Groups, FE.Apps, FE.Coverages, FE.People LEFT JOIN FE.DependentsToEmployees ON FE.People.personId = FE.DependentsToEmployees.dependentId, FE.Offers, FE.Products, FE.CoveredPeople, FE.ElectionTypes
WHERE FE.ElectionTypes.electionTypeId = FE.Coverages.electionTypeId AND FE.Coverages.coverageId = FE.CoveredPeople.coverageId AND FE.CoveredPeople.personId = FE.People.personId AND FE.Coverages.offerId = FE.Offers.offerId
AND FE.Offers.productId = FE.Products.productId  AND FE.Groups.groupId = FE.Apps.groupId AND FE.Apps.appId = FE.Coverages.appId ORDER BY FE.People.personId;