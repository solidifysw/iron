INSERT INTO FE.ElectionTypes (name) VALUES ('enrolled');
INSERT INTO FE.ElectionTypes (name) VALUES ('declined');
INSERT INTO FE.ElectionTypes (name) VALUES ('opt-out');
INSERT INTO `FE`.`Carriers`(`name`,active) VALUES('Assurant',1);
INSERT INTO `FE`.`Carriers`(`name`,active) VALUES('Default',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantStd','STD',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantLtd','LTD',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantVtl','Vol Life',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantAccident','Accident',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantCancer','Cancer',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantCi','Critical Illness',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantGap','GAP',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantDental','Dental',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('assurantVision','Vision',1);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('defaultStd','STD',2);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('defaultLtd','LTD',2);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('defaultMedical','Medical',2);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('defaultVtl','Vol Life',2);
INSERT INTO `FE`.`Products`(`solidifyId`,displayName,carrierId) VALUES('defaultBasicLife','Basic Life',2);
INSERT INTO FE.AppSources (`name`) VALUES ('Web');
INSERT INTO FE.AppSources (`name`) VALUES ('iPad');
INSERT INTO FE.AppSources (`name`) VALUES ('Call Center');
