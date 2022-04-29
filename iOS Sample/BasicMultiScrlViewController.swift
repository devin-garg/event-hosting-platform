//
//  BasicMultiScrlViewController.swift
//  StreamSelfLive
//
//  Created by Devin Garg on 8/6/20.
//  Copyright © 2020 Devin Garg. All rights reserved.
//

import UIKit
internal protocol BasicMultiScrlViewControllerDelegate: NSObjectProtocol {
    func getCameraState()->LiveViewController.CameraCase?
    func setCameraState(state: LiveViewController.CameraCase?)
}
class BasicMultiScrlViewController: UIViewController {
    internal weak var delegate:BasicMultiScrlViewControllerDelegate?
    @IBOutlet var mainContentView: UIView!
    
    @IBOutlet var basicTableView: UITableView!
    
    fileprivate var availableCameraSources = [LiveViewController.CameraCase.front, LiveViewController.CameraCase.back]
    fileprivate var myDidLoadRemovals:[NSObjectProtocol] = []
    deinit {
        for obs in myDidLoadRemovals {
            NotificationCenter.default.removeObserver(obs)
        }
    }
    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        showTheColorOfViews()
    }
    fileprivate var originalMainViewSize:CGFloat?
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
        basicTableView.delegate = self
        basicTableView.dataSource = self
        originalMainViewSize = mainContentView.frame.size.height
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
            
        } else {
            
        }
    }
    fileprivate func showTheColorOfViews(){
        Global.Night.publicChangeIfNight(sets: [mainContentView], isOnDarkMode: Global.Night.isInLightMode())
        lightNightStyling(isInLightMode: Global.Night.isInLightMode())
    }
    fileprivate func lightNightStyling(isInLightMode: Bool) {
        basicTableView.reloadData()
        if isInLightMode {
            basicTableView.backgroundColor = .white
            mainContentView.backgroundColor = .white
        } else {
            basicTableView.backgroundColor = .darkGray
            mainContentView.backgroundColor = .darkGray
        }
    }
    fileprivate func initialViewStyling(){
        
    }
    public func heightOfParentView() -> CGFloat {
        return originalMainViewSize ?? mainContentView.frame.size.height
    }
    public func shouldReloadTV() {
        basicTableView.reloadData()
    }
}

extension BasicMultiScrlViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return availableCameraSources.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "text", for: indexPath) as! BasicTextTableViewCell
        Global.Night.publicChangeIfNight(sets: [cell], isOnDarkMode: Global.Night.isInLightMode())
        switch availableCameraSources[indexPath.row] {
        case .front:
            Global.Style.style(view: cell.textBasicLabel, withStyle: .basicTextLabelFront)
        case .back:
            Global.Style.style(view: cell.textBasicLabel, withStyle: .basicTextLabelBack)
        }
        
        if delegate?.getCameraState() == availableCameraSources[indexPath.row] {
            cell.textBasicLabel.textColor = .black
            cell.contentView.backgroundColor = Global.Style.mainBlueColor
        } else {
            cell.contentView.backgroundColor = Global.Night.isInLightMode()  ? .lightGray : .lightGray
        }
        cell.textBasicLabel.textColor = .white
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: false)
        if delegate?.getCameraState() == availableCameraSources[indexPath.row] {
            delegate?.setCameraState(state: nil)
        } else {
            delegate?.setCameraState(state: availableCameraSources[indexPath.row])
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 50.0
    }
}
