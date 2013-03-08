CREATE TABLE Actor (
    ActorID 		identity 		NOT NULL		PRIMARY KEY,
    ActorName 		varchar(1024) 	NULL
);

CREATE TABLE DataType (
	DataTypeID		identity		NOT NULL		PRIMARY KEY,
	TypeName		varchar(256)	NOT NULL
);

CREATE TABLE ActorVariable (
   	VariableID 		identity 		NOT NULL		PRIMARY KEY,
    ActorID			bigint			NOT NULL		REFERENCES Actor (ActorID),
    VariableName	varchar(256) 	NOT NULL,
    VariableClass 	char(1)			NOT NULL		CHECK VariableClass in ('i', 'o', 's'),
    DataTypeID		bigint			NULL			REFERENCES DataType (DataTypeID)
);

/* comment */
CREATE TABLE DependencyRule (
    RuleID 			identity 		NOT NULL		PRIMARY KEY,
    ActorID			bigint			NOT NULL 		REFERENCES Actor (ActorID),
    SourceVarID 	bigint			NOT NULL 		REFERENCES ActorVariable (VariableID),
    TargetVarID		bigint			NOT NULL 		REFERENCES ActorVariable (VariableID),
    DependencyClass char(3)			NOT NULL		CHECK DependencyClass in ('dep', 'der', 'id', 'val')
);

CREATE TABLE Node (
    NodeID 			identity		NOT NULL		PRIMARY KEY,
    NodeName 		varchar(1024) 	NOT NULL,
    LocalNodeName 	varchar(256) 	NOT NULL,
    ParentNodeID 	bigint			NULL			REFERENCES (NodeID),
    ActorID			bigint			NULL			REFERENCES Actor (ActorID),
    StepCount		bigint			NOT NULL		DEFAULT 0,
    IsHidden		boolean			NOT NULL		DEFAULT FALSE,
    HasChildren		boolean			NOT NULL		DEFAULT FALSE
);
 
CREATE TABLE NodeVariable (
    NodeVariableID 	identity 		NOT NULL		PRIMARY KEY,
   	ActorVariableID	bigint 			NOT NULL		REFERENCES ActorVariable (VariableID),
    NodeID			bigint			NOT NULL		REFERENCES Node (NodeID)
);
 
CREATE TABLE Data(
 	DataID			identity		NOT NULL		PRIMARY KEY,
 	Value			clob			NULL,
 	IsReference		boolean			NOT NULL,
 	DataTypeID		bigint			NULL			REFERENCES DataType (DataTypeID)
);

CREATE TABLE Port (
   	PortID	 		identity 		NOT NULL		PRIMARY KEY,
   	PortName		varchar(256)	NOT NULL,
	NodeID			bigint			NOT NULL		REFERENCES Node (NodeID),
	NodeVariableID	bigint			NULL			REFERENCES NodeVariable (NodeVariableID),
	PortDirection	char(1)			NOT NULL		CHECK (PortDirection in ('i', 'o')),
	UriTemplate		varchar(10240)	NULL,
	PacketCount		bigint			NOT NULL		DEFAULT 0
);

CREATE TABLE Channel(
 	OutPortID		bigint			NOT NULL		REFERENCES Port (PortID),
 	InPortID		bigint			NOT NULL		REFERENCES Port (PortID),
 	PRIMARY KEY (OutPortID, InPortID)
);
 
 CREATE TABLE Step(
 	StepID			identity		NOT NULL		PRIMARY KEY,
 	NodeID			bigint			NOT NULL		REFERENCES Node (NodeID),
 	ParentStepID	bigint			NULL			REFERENCES (StepID),
 	StepNumber		bigint			NULL,
 	UpdateCount		bigint			NOT NULL		DEFAULT 0,
 	StartTime		timestamp		NULL,
 	EndTime			timestamp		NULL
);
 
CREATE TABLE Update(
	UpdateID		identity		NOT NULL		PRIMARY KEY,
	NodeVariableID	bigint			NOT NULL		REFERENCES NodeVariable (NodeVariableID),
	DataID			bigint			NOT NULL		REFERENCES Data (DataID),
	StepID			bigint			NOT NULL		REFERENCES Step (StepID),
	UpdateNumber	bigint			NOT NULL
);

CREATE TABLE PortEvent(
	PortEventID		identity		NOT NULL		PRIMARY KEY,
	PortID			bigint			NOT NULL		REFERENCES Port (PortID),
	PacketID		bigint			NOT NULL,
	StepID			bigint			NULL			REFERENCES Step (StepID),
	EventClass		char(1)			NOT NULL		CHECK EventClass in ('r', 'w'),
	EventNumber		bigint			NOT NULL,
	EventTime		timestamp		NULL
);

CREATE TABLE Packet(
	PacketID		identity		NOT NULL		PRIMARY KEY,
	OriginEventID	bigint			NULL			REFERENCES PortEvent (PortEventID)
);

ALTER TABLE PortEvent ADD FOREIGN KEY (PacketID) 	REFERENCES Packet (PacketID);

CREATE TABLE PacketMetadata(
	MetadataID		identity		NOT NULL		PRIMARY KEY,
	PacketID		bigint			NOT NULL		REFERENCES Packet (PacketID),
	Key				varchar(256)	NOT NULL,
	DataID			bigint			NOT NULL		REFERENCES Data (DataID)
);

CREATE TABLE Resource(
	ResourceID		identity		NOT NULL		PRIMARY KEY,
	Uri				varchar(10240)	NULL,
	DataID			bigint			NULL			REFERENCES Data (DataID)
);

CREATE TABLE PacketResource(
	PacketID		bigint		NOT NULL			REFERENCES Packet (PacketID),
	ResourceID		bigint		NOT NULL			REFERENCES Resource (ResourceID),
	PRIMARY KEY (PacketID, ResourceID)
);

CREATE VIEW PublishedResource AS 
	SELECT Uri, Value, IsReference, DataTypeID, PortEventID
	FROM Data 
		JOIN Resource ON Resource.DataID = Data.DataID
		JOIN PacketResource ON PacketResource.ResourceID = Resource.ResourceID
		JOIN Packet ON Packet.PacketID = PacketResource.PacketID
		JOIN PortEvent ON PortEvent.PacketID = Packet.PacketID	
	WHERE PortEvent.EventClass = 'w'
	ORDER BY Uri, PortID, PortEventID;

CREATE VIEW NodePublishedResource AS 
	SELECT NodeName, ParentNodeID, ActorName, PortName, Step.StepID, StepNumber, Uri, Value, IsReference, DataTypeID
	FROM Data 
		JOIN Resource ON Resource.DataID = Data.DataID
		JOIN PacketResource ON PacketResource.ResourceID = Resource.ResourceID
		JOIN Packet ON Packet.PacketID = PacketResource.PacketID
		JOIN PortEvent ON PortEvent.PacketID = Packet.PacketID
		JOIN Port ON Port.PortID = PortEvent.PortID
		JOIN Node ON Node.NodeID = Port.NodeID
		JOIN Actor ON Actor.ActorID = Node.ActorID
		JOIN Step ON Step.StepID = PortEvent.StepID
	WHERE PortEvent.EventClass = 'w'
	ORDER BY Uri;

CREATE VIEW WorkflowInput AS
	SELECT StepNumber, PortName, Value
	FROM Data 
		JOIN Resource ON Resource.DataID = Data.DataID
		JOIN PacketResource ON PacketResource.ResourceID = Resource.ResourceID
		JOIN Packet ON Packet.PacketID = PacketResource.PacketID
		JOIN PortEvent ON PortEvent.PacketID = Packet.PacketID
		JOIN Port ON Port.PortID = PortEvent.PortID
		JOIN Node ON Node.NodeID = Port.NodeID
		JOIN Step ON Step.StepID = PortEvent.StepID
	WHERE PortEvent.EventClass = 'r' AND ParentNodeID IS NULL
	ORDER BY StepNumber, PortName;
	
CREATE VIEW WorkflowOutput AS
	SELECT StepNumber, PortName, Value
	FROM Data 
		JOIN Resource ON Resource.DataID = Data.DataID
		JOIN PacketResource ON PacketResource.ResourceID = Resource.ResourceID
		JOIN Packet ON Packet.PacketID = PacketResource.PacketID
		JOIN PortEvent ON PortEvent.PacketID = Packet.PacketID
		JOIN Port ON Port.PortID = PortEvent.PortID
		JOIN Node ON Node.NodeID = Port.NodeID
		JOIN Step ON Step.StepID = PortEvent.StepID
	WHERE PortEvent.EventClass = 'w' AND ParentNodeID IS NULL
	ORDER BY StepNumber, PortName;
	
