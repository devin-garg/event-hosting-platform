//
//  AuthViewController.swift
//  StreamSelfLive
//
//  Created by Devin Garg on 8/3/20.
//  Copyright © 2020 Devin Garg. All rights reserved.
//

import UIKit
internal protocol AuthViewControllerDelegate:NSObjectProtocol{
    func authVCStateChanged(authState: AuthViewController.StateOfAuth)
    func errorModel(error: Model.ModelErrors)
}
class AuthViewController: UIViewController {
    internal weak var delegate:AuthViewControllerDelegate?
    @IBOutlet var signedOutView: UIView!
    
    @IBAction func signOutLoginAction(_ sender: Any) {
        let mainViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "ActionAuthViewController") as! ActionAuthViewController
        mainViewController.passedData = ActionAuthViewController.PassedData(context: .Signin)
        present(mainViewController, animated: true, completion: nil)
    }
    @IBOutlet var signoutLabel: UILabel!
    @IBOutlet var signedOutLogin: UIButton!
    @IBOutlet var signedInAttendeeView: UIView!
    
    @IBAction func signedInAttendeeLogoutAction(_ sender: Any) {
        Model.signOut()
    }
    @IBOutlet var signedInAttendeeLabel: UILabel!
    @IBOutlet var signedInAttendeeLogout: UIButton!
    
    
    @IBOutlet var signedInCreatorView: UIView!
    
    @IBAction func signedInCreatorLogoutAction(_ sender: Any) {
        Model.signOut()
    }
    @IBOutlet var signedInCreatorLabel: UILabel!
    @IBOutlet var signedInCreatorLogout: UIButton!
    @IBOutlet var waitingView: UIView!
    @IBOutlet var waitingHolderView: UIView!
    
    @IBOutlet var errorView: UIView!
    
    @IBAction func errorRetryActionBtn(_ sender: Any) {
        runAuth()
    }
    @IBOutlet var errorRetryBtn: UIButton!
    

    enum StateOfAuth {
        case Waiting
        case Error
        case SignedInAttendee
        case SignedOut
        case SignedInCreator
    }
    
    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        showTheColorOfViews()
    }
    fileprivate lazy var waitingSpinner = Global.Waiting.init(hostView: waitingHolderView)
    fileprivate var myDidLoadRemovals:[NSObjectProtocol] = []
    deinit {
        for obs in myDidLoadRemovals {
            NotificationCenter.default.removeObserver(obs)
        }
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        let myDarkModeObs = NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: Global.Radio.nightModeNotif), object: nil, queue: OperationQueue.main) {[weak weakSelf = self] (_) in
            guard let strongSelf = weakSelf else{
                return
            }
            strongSelf.showTheColorOfViews()
            
        }
        myDidLoadRemovals.append(myDarkModeObs)
        let signInObs = NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: Global.Radio.signInStateNotif), object: nil, queue: OperationQueue.main) {[weak weakSelf = self] (_) in
            guard let strongSelf = weakSelf else{
                return
            }
            strongSelf.authRes(error: GlobalModelUserInfo.0, result: GlobalModelUserInfo.1)
            
        }
        myDidLoadRemovals.append(signInObs)
        
        initialViewStyling()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        willAppearDis(appear: true)
    }
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        willAppearDis(appear: false)
    }
    public func willAppearDis(appear:Bool) {
        if appear {
            showTheColorOfViews()
            waitingSpinner.isAppeared(appeared: true)
        } else {
            waitingSpinner.isAppeared(appeared: false)
        }
    }
    fileprivate func showTheColorOfViews(){
        Global.Night.publicChangeIfNight(sets: [signedOutView, signedInCreatorView, signedInAttendeeView, waitingView, errorView], isOnDarkMode: Global.Night.isInLightMode())
        lightNightStyling(isInLightMode: Global.Night.isInLightMode())
    }
    fileprivate func lightNightStyling(isInLightMode: Bool) {
        if isInLightMode {
            signedOutView.backgroundColor = .white
            signedInAttendeeView.backgroundColor = .white
            signedInCreatorView.backgroundColor = .white
            waitingView.backgroundColor = .white
            waitingHolderView.backgroundColor = .white
            errorView.backgroundColor = .white
        } else {
            signedOutView.backgroundColor = .black
            signedInAttendeeView.backgroundColor = .black
            signedInCreatorView.backgroundColor = .black
            waitingView.backgroundColor = .black
            waitingHolderView.backgroundColor = .black
            errorView.backgroundColor = .black
        }
    }
    fileprivate func initialViewStyling(){
        Global.Style.style(view: signoutLabel, withStyle: .signInStateInfoLabel)
        Global.Style.style(view: signedOutLogin, withStyle: .signInOutButton)
        Global.Style.style(view: signedInAttendeeLabel, withStyle: .signInStateInfoLabel)
        Global.Style.style(view: signedInAttendeeLogout, withStyle: .signInOutButton)
        Global.Style.style(view: signedInCreatorLabel, withStyle: .signInStateInfoLabel)
        Global.Style.style(view: signedInCreatorLogout, withStyle: .signInOutButton)
        Global.Style.style(view: errorRetryBtn, withStyle: .retryBtn)
    }
    public func stateOfAuth(auth: StateOfAuth) {
        switch auth {
        case .Waiting:
            waitingSpinner.isWaiting(isUp: true)
        case .Error:
            waitingSpinner.isWaiting(isUp: false)
        case .SignedInAttendee:
            waitingSpinner.isWaiting(isUp: false)
        case .SignedOut:
            waitingSpinner.isWaiting(isUp: false)
        case .SignedInCreator:
            waitingSpinner.isWaiting(isUp: false)
        }
    }
    fileprivate func authRes(error: Model.ModelErrors?, result: Model.UserInfo?){
        if let error = error {
            delegate?.authVCStateChanged(authState: .Error)
            delegate?.errorModel(error: error)
        } else {
            if let result = result  {
                if let uname = result.username {
                    signedInCreatorLabel.text = "Hi \(uname)!"
                    if Global.Config.isMockTesting {
                        signedInCreatorLabel.text = "Hi GuitarH3ro!"
                    }
                    delegate?.authVCStateChanged(authState: .SignedInCreator)
                } else {
                    signedInAttendeeLabel.text = "Hi Friend! (\(result.email))"
                    delegate?.authVCStateChanged(authState: .SignedInAttendee)
                }
            } else {
                delegate?.authVCStateChanged(authState: .SignedOut)
            }
        }
    }
    public func runAuth(){
        delegate?.authVCStateChanged(authState: .Waiting)
        Model.requestingSignInState()
    }
}
