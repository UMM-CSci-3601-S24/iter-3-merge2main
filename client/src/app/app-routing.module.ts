import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { HostProfileComponent } from './hosts/host-profile.component';
import { HuntProfileComponent } from './hunts/hunt-profile.component';
import { HunterViewComponent } from './hunters/join-hunt/hunter-view.component';
import { HuntCardComponent } from './hunts/hunt-card.component';
import { AddHuntComponent } from './hunts/addHunt/add-hunt.component';
import { JoinHuntComponent } from './hunters/join-hunt/join-hunt.component';
import { StartHuntComponent } from './startHunt/start-hunt.component';
import { EndedHuntDetailsComponent } from './endedHunts/endedHuntDetails/ended-hunt.details.component';
import { HunterEndedHuntComponent } from './hunters/hunter-ended-hunt/hunter-ended-hunt.component';
import { AddTeamsComponent } from './teams/add-teams/add-teams.component';
import { SelectTeamComponent } from './select-team/select-team.component';
// Note that the 'users/new' route needs to come before 'users/:id'.
// If 'users/:id' came first, it would accidentally catch requests to
// 'users/new'; the router would just think that the string 'new' is a user ID.
const routes: Routes = [
  {path: '', component: HomeComponent, title: 'Home'},
  {path: 'hosts', component: HostProfileComponent, title: 'Host Profile'},
  {path: 'hunts/new', component: AddHuntComponent, title: 'Add Hunt'},
  {path: 'hunts/:id', component: HuntProfileComponent, title: 'Hunts Profile'},
  {path: 'hunts', component: HuntCardComponent, title: 'Hunts'},
  {path: 'hunters', component: JoinHuntComponent, title: 'Join Hunt'},
  {path: 'hunter-view/1', component: HunterEndedHuntComponent, title: 'Ended Hunt View'},
  {path: 'hunter-view/:accessCode/teams', component: SelectTeamComponent, title: 'Select Team'},
  {path: 'hunter-view/:accessCode/teams/:teamId', component: HunterViewComponent},
  {path: 'startedHunts/:accessCode', component: StartHuntComponent, title: 'Started Hunt'},
  {path: 'startedHunts/:accessCode/addTeams', component: AddTeamsComponent, title: 'Add Teams'},
  {path: 'endedHunts/:id', component: EndedHuntDetailsComponent, title: 'Ended Hunt Details'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
