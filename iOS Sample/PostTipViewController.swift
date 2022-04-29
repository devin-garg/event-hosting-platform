//
//  PostTipViewController.swift
//  StreamSelfLive
//
//  Created by Devin Garg on 8/11/20.
//  Copyright © 2020 Devin Garg. All rights reserved.
//

import UIKit
protocol PostTipViewControllerDelegate: NSObjectProtocol {
    
}
class PostTipViewController: UIViewController {
    internal weak var delegate: PostTipViewControllerDelegate?
    @IBOutlet var mainScrollView: UIScrollView!
    
    @IBOutlet var backBtn: UIButton!

    @IBAction func backAction(_ sender: Any) {
        Model.addPostTip(liveid: passedData.liveid, feedback: nil, payment: nil) {_,_ in }
        presentingViewController?.dismiss(animated: true, completion: nil)
    }
    @IBOutlet var addTipFeedbackHeader: UILabel!
    
    @IBOutlet var tipView: UIView!
    
    @IBOutlet var tipViewHeight: NSLayoutConstraint!
    
    @IBOutlet var tipHeader: UILabel!
    
    @IBOutlet var feedbackHeader: UILabel!
    
    @IBOutlet var feedbackTextView: UITextView!
    
    @IBOutlet var topSpacingPad: NSLayoutConstraint!
    
    
    fileprivate var isSubmitWorking = false
    @IBAction func submitBtnAction(_ sender: Any) {
        if isSubmitWorking {
            return
        }
        isSubmitWorking = true
        feedbackTextView.resignFirstResponder()
        subTipViewController.requestTipMethod {[weak weakSelf = self] (payment:String?, err:Model.ModelErrors?) in
            DispatchQueue.main.async {
                guard let strongSelf = weakSelf else {
                    return
                }
                if let err = err {
                    strongSelf.isSubmitWorking = false
                    Global.ErrorHandling.addMyRes(myMessage: err.rawValue, myView: strongSelf.view, isGood: false)
                } else {
                    let existingText = (strongSelf.feedbackTextView.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
                    if !existingText.isEmpty || payment != nil {
                        Model.addPostTip(liveid: strongSelf.passedData.liveid, feedback: existingText, payment: payment) {[weak weakSelf = strongSelf] (err:Model.ModelErrors?, signIn:Bool) in
                            DispatchQueue.main.async {
                                guard let strongSelf = weakSelf else {
                                    return
                                }
                                if signIn {
                                    strongSelf.isSubmitWorking = false
                                    let mainViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "ActionAuthViewController") as! ActionAuthViewController
                                      mainViewController.passedData = ActionAuthViewController.PassedData(context: .Signin)
                                      strongSelf.present(mainViewController, animated: true, completion: nil)
                                } else {
                                    if let err = err {
                                        strongSelf.isSubmitWorking = false
                                        Global.ErrorHandling.addMyRes(myMessage: err.rawValue, myView: strongSelf.view, isGood: false)
                                    } else {
                                        strongSelf.isSubmitWorking = false
                                        strongSelf.submitBtn.isUserInteractionEnabled = false
                                        strongSelf.backBtn.isUserInteractionEnabled = false
                                        Global.ErrorHandling.addMyRes(myMessage: Global.ResStrings.ResMsg.Success.rawValue, myView: strongSelf.view, isGood: true)
                                        Timer.scheduledTimer(withTimeInterval: 3.0, repeats: false) {[weak weakSelf = strongSelf] (_) in
                                            DispatchQueue.main.async {
                                                   guard let strongSelf = weakSelf else {
                                                       return
                                                   }
                                                strongSelf.presentingViewController?.dismiss(animated: true, completion: nil)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        strongSelf.isSubmitWorking = false
                        Global.ErrorHandling.addMyRes(myMessage: Global.ResStrings.ResMsg.noInput.rawValue, myView: strongSelf.view, isGood: false)
                    }
                }
            }
        }
    }
    
    @IBOutlet var submitBtn: UIButton!
    
    @IBOutlet var titleOfLive: UILabel!
    
    
    @IBOutlet var authorOfLive: UILabel!
    
    
    
    
    var passedData:PassedData!
    
    struct PassedData {
        let title: String
        let author: String
        let liveid: String
    }
    fileprivate var myDidLoadRemovals:[NSObjectProtocol] = []
    lazy var subTipViewController: SubTipViewController = {
        let mainViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SubTipViewController") as! SubTipViewController
        mainViewController.delegate = self
        return mainViewController
    }()
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
        initialViewStyling()
        add(asChildViewController: subTipViewController)
        let _ = subTipViewController.view
        let height = subTipViewController.heightOfParentView()
        tipViewHeight.constant = height
        Global.Utility.addSubviewFull(inView: tipView, withView: subTipViewController.mainContentView)
        feedbackTextView.delegate = self
        titleOfLive.text = passedData.title
        authorOfLive.text = "@\(passedData.author)"
        feedbackTextView.layer.borderWidth = 1.0
        feedbackTextView.layer.borderColor = Global.Style.mainGreenColor.cgColor
        if Global.Config.isMockTesting {
            feedbackTextView.text = "Thank you for the discussion, I learned a lot. I really like that you listened to my personal story on how to be promoted within my own group. I also value the other participants on the video call that I got to hear their perspective. Your media presentation as well were in-depth too. I am reccommending this to my friends going through similar situations. All the best and will connect again soon."
        }
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
            subTipViewController.willAppearDis(appear: true)
            NotificationCenter.default.addObserver(self, selector:#selector(self.keyboardWillAppear(notification:)), name: UIResponder.keyboardWillShowNotification, object: nil)
            NotificationCenter.default.addObserver(self, selector:#selector(self.keyboardWillDisappear(notification:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        } else {
            subTipViewController.willAppearDis(appear: false)
            NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillShowNotification, object: nil)
            NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
        }
    }
    fileprivate func showTheColorOfViews(){
        Global.Night.publicChangeIfNight(sets: [view], isOnDarkMode: Global.Night.isInLightMode())
        lightNightStyling(isInLightMode: Global.Night.isInLightMode())
    }
    fileprivate func lightNightStyling(isInLightMode: Bool) {
        if isInLightMode {
            view.backgroundColor = .white
            tipView.backgroundColor = .white
        } else {
            view.backgroundColor = .darkGray
            tipView.backgroundColor = .darkGray
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        changePlacement()
    }
    fileprivate func changePlacement(){
        Timer.scheduledTimer(withTimeInterval: 0.0, repeats: false) {[weak weakSelf = self] (_) in
            DispatchQueue.main.async {
                guard let strongSelf = weakSelf else {
                    return
                }
                switch UIDevice.current.userInterfaceIdiom {
                case .unspecified:
                    break
                case .phone:
                    break
                case .pad:
                    if strongSelf.submitBtn.frame.origin.y + strongSelf.submitBtn.frame.size.height < strongSelf.view.frame.size.height {
                        strongSelf.topSpacingPad.constant = 10.0 + (strongSelf.view.frame.size.height - (strongSelf.submitBtn.frame.origin.y + strongSelf.submitBtn.frame.size.height)) / 2.0
                    } else {
                        strongSelf.topSpacingPad.constant = 10.0
                    }
                    Timer.scheduledTimer(withTimeInterval: 0.0, repeats: false) {[weak weakSelf = strongSelf] (_) in
                        DispatchQueue.main.async {
                            guard let strongSelf = weakSelf else {
                                return
                            }
                            strongSelf.localRelayOut()
                        }
                    }
                case .tv:
                    break
                case .carPlay:
                    break
                @unknown default:
                    break
                }
            }
        }
    }
    fileprivate func initialViewStyling(){
        Global.Style.style(view: backBtn, withStyle: .backBtn)
        Global.Style.style(view: addTipFeedbackHeader, withStyle: .postTipMainHeader)
//        @IBOutlet var tipViewHeight: NSLayoutConstraint!
        Global.Style.style(view: tipHeader, withStyle: .postTipSectionHeader)
        Global.Style.style(view: feedbackHeader, withStyle: .postTipSectionHeader)
//        @IBOutlet var feedbackTextView: UITextView!
        Global.Style.style(view: submitBtn, withStyle: .mainBtnLarge)
        Global.Style.style(view: titleOfLive, withStyle: .title)
        Global.Style.style(view: authorOfLive, withStyle: .usernameLabel)
        
    }
    
    fileprivate var keyboardIsShown = false
    fileprivate var keyboardHeight:CGFloat?
    @objc func keyboardWillAppear(notification: NSNotification){
        keyboardIsShown = true
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            keyboardHeight = keyboardFrame.cgRectValue.height
            localRelayOut()
        }
    }
    override func didRotate(from fromInterfaceOrientation: UIInterfaceOrientation) {
        localRelayOut()
        changePlacement()
    }
    @objc func keyboardWillDisappear(notification: NSNotification){
        keyboardIsShown = false
        localRelayOut()
    }
    fileprivate func localRelayOut(){
        if let keyboardHeight = keyboardHeight {
            mainScrollView.contentSize.height = submitBtn.frame.origin.y + submitBtn.frame.size.height + 20.0 + (keyboardIsShown ? keyboardHeight : 0.0) // 20.0 is the spacing
            if keyboardIsShown {
                mainScrollView.scrollToBottom(animated: true)
            }
        }
    }
    
    
    func add(asChildViewController viewController: UIViewController) {
        // Add Child View Controller
        addChild(viewController)
        
        // Notify Child View Controller
        viewController.didMove(toParent: self)
    }
}

extension PostTipViewController: SubTipViewControllerDelegate {
    
}
extension PostTipViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if(text == "\n") {
            textView.resignFirstResponder()
            return false
        }
        let newText = (textView.text as NSString).replacingCharacters(in: range, with: text)
        if newText.count <= 300 {
            return true
        }
        return false
    }
}
