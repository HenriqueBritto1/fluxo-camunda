export interface CamundaTask {
    id: string;
    name: string;
    assignee?: string;
    created: string;
    processInstanceId: string;
    taskDefinitionKey: string;
}