export class BeginHuntPage {
  private readonly baseUrl = '/hosts';
  private readonly BeginHuntButton = '.begin-hunt';
  private readonly hostCardSelector = '.hunt-cards-container app-hunt-card';
  private readonly profileButtonSelector = '[data-test=viewProfileButton]';
  private readonly huntAccessCode = '.access-code-number';
  private readonly SecondBeginHuntButton = '.begin-hunt-button';
  private readonly endHuntButton = '.end-hunt-button';
  private readonly huntTaskList = '.task-list';
  private readonly tableTaskTitle = '.flex-1';
  private readonly progressTeamTile = '.flex-2';
  private readonly teamCard = '.team-card';

  navigateTo() {
    return cy.visit(this.baseUrl);
  }

  /**
   * @returns The begin hunt button
   */
  beginHuntButton() {
    return cy.get(this.BeginHuntButton);
  }

  /**
   * @returns The hunt cards on host page
   */
  getHuntCards() {
    return cy.get(this.hostCardSelector);
  }

  /**
   * @returns click view profile button on card
   */
  clickViewProfile(card: Cypress.Chainable<JQuery<HTMLElement>>) {
    return card.find<HTMLButtonElement>(this.profileButtonSelector).click();
  }

  /**
   * Get the access code from the started Hunt.
   * Requires begin Hunt in the "hunt" view as hosts.
   *
   * @returns the value of the element with the class `.col-md-12 Access Code`
   */
  getAccessCode() {
    return cy.get(this.huntAccessCode).invoke('text').as('accessCode');
  }

  /**
   * Click the second begin hunt button that start the hunt
   *
   * @returns the second begin hunt button
   */
  clickSecondBeginHuntButton() {
    return cy.get(this.SecondBeginHuntButton).click();
  }

  /**
   * Click the end hunt button that end the hunt
   *
   * @return the end hunt button
   */
  clickEndHuntButton() {
    return cy.get(this.endHuntButton).click();
  }

  /**
   * Get the task list of the hunt as hunter-view.
   *
   * @return the value of the element with the class `.task-list`
   */
  getHuntTaskList() {
    return cy.get(this.huntTaskList);
  }

  /**
   * Get the task title from the table
   *
   * @return the value of the element with the class `.flex-1`
   */
  getTableTaskTitle() {
    return cy.get(this.tableTaskTitle);
  }

  /**
   * Get the progress team tile from the table
   *
   * @return the value of the element with the class `.flex-2`
   */
  getProgressTeamTile() {
    return cy.get(this.progressTeamTile);
  }

  /**
   * Get the team card
   *
   * @return the value of the element with the class `.team-card`
   */
  getTeamCard() {
    return cy.get(this.teamCard);
  }

  /**
   * Get a started hunt's id
   *
   * @returns the id of the started hunt
   */
  getStartedHuntId(accessCode: string) {
    return cy.request('GET', `http://localhost:4200/api/startedHunts/${accessCode}`).its('body').then((startedHunt) => {
      console.log(startedHunt);
      if (startedHunt && startedHunt._id) {
        return startedHunt._id;
      } else {
        throw new Error('No started hunt found');
      }
    });
  }
}
