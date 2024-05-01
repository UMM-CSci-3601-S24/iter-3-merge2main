export class JoinHuntPage {
  private readonly baseUrl = '/hunters';
  private readonly pageTitle = '.title';
  private readonly joinHuntButtonSelector = '[name="join-button"]';
  private readonly accessCodeInputField = '.input-container';
  private readonly appPageTitle = '.app-title';
  private readonly HomeButton = '[name="home-button"]';
  private readonly HunterButton = '[name="hunter-button"]';
  private readonly HostButton = '[name="host-button"]';
  private readonly hostCardSelector = '.hunt-cards-container app-hunt-card';
  private readonly profileButtonSelector = '[data-test=viewProfileButton]';
  private readonly beginHuntButton = '.begin-hunt';
  private readonly huntAccessCode = '.access-code-number';
  private readonly teamSelectButton = '.team-select';
  private readonly hunterProceedButton = '.hunter-proceed-button';
  private readonly teamNumberSelector = '.team-number';
  private readonly proceedButton = '.proceed-button';


  navigateTo() {
    return cy.visit(this.baseUrl);
  }

  /**
   * Gets the title of the app when visiting the `/join-hunt` page.
   *
   * @returns the value of the element with the ID `.join-hunt-title`
   */
  getJoinHuntTitle() {
    return cy.get(this.pageTitle);
  }

/**
 * Get the join-hunt button DOM element.
 *
 * @returns an iterable (`Cypress.Chainable`) containing the `join-hunt` DOM element.
 */
  getJoinHuntButton() {
    return cy.get(this.joinHuntButtonSelector);
  }

  /**
   * Type in the access code input field.
   *
   * @param accessCode The access code to type in the input field.
   */
  getAccessCodeInputField() {
    return cy.get(this.accessCodeInputField);
  }

  /**
   * Get the app page title when visiting the `/app` page.
   *
   * @returns the value of the element with the ID `.app-title`
   */
  getAppTitle(){
    return cy.get(this.appPageTitle);
  }

  /**
   * Get the Home button DOM element.
   *
   * @returns the value of the element with the ID `.home-button`
   */
  getHomeButton(){
    return cy.get(this.HomeButton);
  }

  /**
   * Get the Hunter button DOM element.
   *
   * @returns the value of the element with the class `.hunter-button`
   */
  getHunterButton(){
    return cy.get(this.HunterButton);
  }

  /**
   * Get the position of the box in the access input field.
   *
   * @param index
   * @returns
   */
  getAccessCodeInput(index: number) {
    return cy.get(`.input-container input:nth-child(${index})`);
  }

  /**
   * Get the host button DOM element.
   *
   * @return the value of the element with the ID `[name="host-button"]`
   */
  getHostButton() {
    return cy.get(this.HostButton);
  }

  /**
   * Get all the `app-host-card` DOM elements. This will be
   * empty if we're using the list view of the hosts.
   *
   * @returns an iterable (`Cypress.Chainable`) containing all
   *   the `app-host-card` DOM elements.
   */
  getHuntCards() {
    return cy.get(this.hostCardSelector);
  }

  /**
   * Clicks the "view profile" button for the given host card.
   * Requires being in the "card" view.
   *
   * @param card The host card
   */
  clickViewProfile(card: Cypress.Chainable<JQuery<HTMLElement>>) {
    return card.find<HTMLButtonElement>(this.profileButtonSelector).click();
  }

  /**
   * Clicks the "begin hunt" button in the hunt page.
   * Requires being in the "hunt" view as hosts.
   *
   * @returns the value of the element with the ID `.begin-hunt`
   */
  clickBeginHunt() {
    return cy.get(this.beginHuntButton).click();
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

  clickTeamButton() {
    return cy.get(this.teamSelectButton).click();
  }

  clickProceedButton() {
    return cy.get(this.hunterProceedButton).click();
  }

  /**
   * Select the team number for the hunt.
   *
   * @param teamNumber
   */
  selectTeamNumber(teamNumber: number) {
    return cy.get(this.teamNumberSelector).select(teamNumber.toString());
  }

  /**
   * Click the proceed button.
   */
  clickHunterProceedButton() {
    return cy.get(this.proceedButton).click();
  }
}
